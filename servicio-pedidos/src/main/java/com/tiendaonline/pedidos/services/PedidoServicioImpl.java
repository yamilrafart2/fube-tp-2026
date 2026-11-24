package com.tiendaonline.pedidos.services;

import com.tiendaonline.pedidos.clients.ClientesClient;
import com.tiendaonline.pedidos.clients.InventarioClient;
import com.tiendaonline.pedidos.clients.dtos.ReservaInventarioResponseDTO;
import com.tiendaonline.pedidos.dtos.ItemPedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoResponseDTO;
import com.tiendaonline.pedidos.exceptions.ConflictoException;
import com.tiendaonline.pedidos.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.exceptions.ServicioNoDisponibleException;
import com.tiendaonline.pedidos.mappers.PedidoMapper;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.ItemPedido;
import com.tiendaonline.pedidos.models.Pedido;
import com.tiendaonline.pedidos.repositories.PedidoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoServicioImpl implements PedidoServicio {

    private final PedidoRepositorio pedidoRepositorio;

    private final ClientesClient clientesClient;

    private final InventarioClient inventarioClient;

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO dto) {
        clientesClient.verificarClienteExiste(dto.getClienteId());

        Pedido entidad = PedidoMapper.toPedidoEntity(dto);

        Pedido pedidoGuardado = pedidoRepositorio.save(entidad);

        BigDecimal calculoMontoTotal = BigDecimal.ZERO;

        int reservasRealizadas = 0;

        try {
            for (ItemPedidoRequestDTO itemDto : dto.getListaItems()) {
                ReservaInventarioResponseDTO reservaDTO = inventarioClient.reservarStock(
                        itemDto.getProductoId(),
                        pedidoGuardado.getId(),
                        itemDto.getCantidad());

                reservasRealizadas++;

                ItemPedido itemPedido = PedidoMapper.toItemEntity(itemDto, reservaDTO.getPrecioUnitario());

                pedidoGuardado.agregarItem(itemPedido);

                calculoMontoTotal = calculoMontoTotal.add(
                        itemPedido.getPrecioUnitario().multiply(BigDecimal.valueOf(itemPedido.getCantidad())));
            }
        } catch (RuntimeException ex) {
            if (reservasRealizadas > 0) {
                try {
                    log.warn("Fallo al reservar stock; ejecutando compensación {}", kv("pedidoId", pedidoGuardado.getId()));
                    inventarioClient.liberarReservasPorPedido(pedidoGuardado.getId());
                } catch (Exception e) {
                    log.error("Error al compensar las reservas del pedido {}: {}",
                            pedidoGuardado.getId(), e.getMessage());
                }
            }
            throw ex;
        }

        pedidoGuardado.setMontoTotal(calculoMontoTotal);

        pedidoGuardado = pedidoRepositorio.save(pedidoGuardado);

        log.info("Pedido creado {} {} {}",
                kv("pedidoId", pedidoGuardado.getId()),
            kv("clienteId", pedidoGuardado.getClienteId()),
            kv("montoTotal", pedidoGuardado.getMontoTotal()));

        return PedidoMapper.toPedidoDTO(pedidoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPedidoPorId(Long id) {
        Pedido pedido = pedidoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido con id " + id + " no encontrado"));

        return PedidoMapper.toPedidoDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidosPorCliente(Long clienteId, EstadoPedido estado) {
        if (estado == null) {
            return pedidoRepositorio.findByClienteIdOrderByFechaCreacionDesc(clienteId)
                    .stream()
                    .map(PedidoMapper::toPedidoDTO)
                    .toList();
        }

        return pedidoRepositorio.findByClienteIdAndEstadoOrderByFechaCreacionDesc(clienteId, estado)
                .stream()
                .map(PedidoMapper::toPedidoDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponseDTO confirmarPedido(Long id) {
        Pedido pedido = pedidoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido con id " + id + " no encontrado"));

        if (!pedido.getEstado().equals(EstadoPedido.CREADO)) {
            throw new ConflictoException("TRANSICION_INVALIDA",
                    "Solo se pueden confirmar pedidos en estado CREADO. Estado actual: " + pedido.getEstado());
        }

        pedido.setEstado(EstadoPedido.CONFIRMADO);

        Pedido actualizado = pedidoRepositorio.save(pedido);
        log.info("Pedido confirmado {}", kv("pedidoId", id));

        return PedidoMapper.toPedidoDTO(actualizado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO cancelarPedido(Long id) {
        Pedido pedido = pedidoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido con id " + id + " no encontrado"));

        if (!pedido.getEstado().equals(EstadoPedido.CREADO) && !pedido.getEstado().equals(EstadoPedido.CONFIRMADO)) {
            throw new ConflictoException("TRANSICION_INVALIDA",
                    "No se puede cancelar un pedido en estado: " + pedido.getEstado());
        }

        inventarioClient.liberarReservasPorPedido(pedido.getId());

        pedido.setEstado(EstadoPedido.CANCELADO);

        Pedido actualizado = pedidoRepositorio.save(pedido);
        log.info("Pedido cancelado y reservas liberadas {}", kv("pedidoId", id));

        return PedidoMapper.toPedidoDTO(actualizado);
    }

}

