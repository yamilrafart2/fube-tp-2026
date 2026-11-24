package com.tiendaonline.inventario.services;

import com.tiendaonline.inventario.dtos.*;
import com.tiendaonline.inventario.exceptions.ConflictoException;
import com.tiendaonline.inventario.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.inventario.mappers.InventarioMapper;
import com.tiendaonline.inventario.models.EstadoReserva;
import com.tiendaonline.inventario.models.Producto;
import com.tiendaonline.inventario.models.Reserva;
import com.tiendaonline.inventario.repositories.ProductoRepositorio;
import com.tiendaonline.inventario.repositories.ReservaRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServicioImpl implements InventarioServicio {

    private final ProductoRepositorio productoRepositorio;

    private final ReservaRepositorio reservaRepositorio;

    @Transactional
    @Override
    public ProductoResponseDTO crearProducto(ProductoRequestDTO dto) {
        if (productoRepositorio.existsBySku(dto.getSku())) {
            throw new ConflictoException("SKU_DUPLICADO",
                    "Ya existe un producto registrado con el SKU: " + dto.getSku());
        }

        Producto nuevo = InventarioMapper.toProductoEntity(dto);
        Producto guardado = productoRepositorio.save(nuevo);
        log.info("Producto creado {} {}", kv("productoId", guardado.getId()), kv("sku", guardado.getSku()));

        return InventarioMapper.toProductoDTO(guardado);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductoResponseDTO obtenerProductoPorId(Long id) {
        log.debug("Producto consultado {}", kv("productoId", id));
        Producto producto = productoRepositorio.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró el producto con ID: " + id));

        return InventarioMapper.toProductoDTO(producto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductoResponseDTO> listarProductos(Pageable pageable) {
        return productoRepositorio.findAll(pageable).map(InventarioMapper::toProductoDTO);
    }


    @Transactional
    @Override
    public ReservaResponseDTO reservarStock(ReservaRequestDTO dto) {
        Producto producto = productoRepositorio.findById(dto.getProductoId()).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró el producto con ID: " +
                        dto.getProductoId()));

        int filasActualizadas = productoRepositorio.descontarStock(dto.getProductoId(), dto.getCantidad());

        if (filasActualizadas == 0) {
            log.warn("Stock insuficiente para reserva {} {} {}",
                    kv("productoId", dto.getProductoId()),
                    kv("pedidoId", dto.getPedidoId()),
                    kv("cantidad", dto.getCantidad()));
            throw new ConflictoException("STOCK_INSUFICIENTE",
                    "Stock insuficiente para el producto con ID: " + dto.getProductoId() +
                            ". Disponible: " + producto.getCantidadDisponible() +
                            ", solicitado: " + dto.getCantidad());
        }

        Reserva reserva = InventarioMapper.toReservaEntity(dto);
        Reserva guardada = reservaRepositorio.save(reserva);

        log.info("Stock reservado {} {} {} {}",
                kv("reservaId", guardada.getId()),
                kv("productoId", guardada.getProductoId()),
                kv("pedidoId", guardada.getPedidoId()),
                kv("cantidad", guardada.getCantidad()));

        return InventarioMapper.toReservaDTO(guardada, producto.getPrecio());
    }

    @Transactional
    @Override
    public ReservaResponseDTO liberarReserva(Long reservaId) {
        Reserva reserva = reservaRepositorio.findById(reservaId).orElseThrow(
                () -> new RecursoNoEncontradoException("No se encontró la reserva con ID: " + reservaId));

        if (reserva.getEstado() == EstadoReserva.LIBERADA) {
            return InventarioMapper.toReservaDTO(reserva, null);
        }

        reserva.setEstado(EstadoReserva.LIBERADA);
        Reserva actualizada = reservaRepositorio.save(reserva);

        productoRepositorio.incrementarStock(reserva.getProductoId(), reserva.getCantidad());

        log.info("Reservas liberadas {} {} {}",
                kv("reservaId", actualizada.getId()),
                kv("pedidoId", actualizada.getPedidoId()),
                kv("cantidad", actualizada.getCantidad()));

        return InventarioMapper.toReservaDTO(actualizada, null);
    }


    @Transactional
    @Override
    public LiberacionPorPedidoResponseDTO liberarReservasPorPedido(LiberacionPorPedidoRequestDTO dto) {
        List<Reserva> activas = reservaRepositorio.findByPedidoIdAndEstado(dto.getPedidoId(), EstadoReserva.ACTIVA);

        for (Reserva reserva : activas) {
            reserva.setEstado(EstadoReserva.LIBERADA);
            reservaRepositorio.save(reserva);
            productoRepositorio.incrementarStock(reserva.getProductoId(), reserva.getCantidad());
        }

        log.info("Reservas liberadas {} {}",
                kv("pedidoId", dto.getPedidoId()),
                kv("reservasLiberadas", activas.size()));

        return LiberacionPorPedidoResponseDTO.builder()
                .pedidoId(dto.getPedidoId())
                .reservasLiberadas(activas.size())
                .build();
    }
}
