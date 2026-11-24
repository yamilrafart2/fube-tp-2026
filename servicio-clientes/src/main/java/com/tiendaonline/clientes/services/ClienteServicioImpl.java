package com.tiendaonline.clientes.services;

import com.tiendaonline.clientes.clients.PedidosClient;
import com.tiendaonline.clientes.clients.dtos.PedidoClienteResponseDTO;
import com.tiendaonline.clientes.dtos.ClienteRequestDTO;
import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import com.tiendaonline.clientes.exceptions.ConflictoException;
import com.tiendaonline.clientes.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.clientes.mappers.ClienteMapper;
import com.tiendaonline.clientes.models.Cliente;
import com.tiendaonline.clientes.repositories.ClienteRepositorio;
import com.tiendaonline.clientes.utils.EnmascaradorPii;
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
public class ClienteServicioImpl implements ClienteServicio {

    private final ClienteRepositorio clienteRepositorio;
    private final FeatureFlagServicio featureFlagServicio;
    private final PedidosClient pedidosClient;

    // Registra un nuevo cliente en el sistema
    @Transactional
    @Override
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        // El email debe ser único
        if (clienteRepositorio.existsByEmail(request.getEmail())) {
            log.warn("Intento de alta con email duplicado");
            throw new ConflictoException("EMAIL_DUPLICADO",
                    "Ya existe un cliente registrado con el email: " + request.getEmail());
        }

        Cliente nuevo = ClienteMapper.toEntity(request);
        Cliente guardado = clienteRepositorio.save(nuevo);
        log.info("Cliente creado {}", kv("clienteId", guardado.getId()));

        return mapearYEnmascararSegunFlag(guardado);
    }

    // Busca un cliente por su ID
    @Transactional(readOnly = true)
    @Override
    public ClienteResponseDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado {}", kv("clienteId", id));
                    return new RecursoNoEncontradoException("Cliente con id " + id + " no encontrado");
                });

        log.info("Cliente consultado {}", kv("clienteId", id));

        return mapearYEnmascararSegunFlag(cliente);
    }

    // Lista los clientes de forma paginada, evaluando para cada uno el feature flag mask-pii
    @Transactional(readOnly = true)
    @Override
    public Page<ClienteResponseDTO> listarClientes(Pageable pageable) {
        return clienteRepositorio.findAll(pageable)
                .map(this::mapearYEnmascararSegunFlag);
    }

    // Consulta los pedidos de un cliente
    @Override
    @Transactional(readOnly = true)
    public List<PedidoClienteResponseDTO> obtenerPedidosDeCliente(Long id) {
        log.info("Consultando pedidos del cliente {}", kv("clienteId", id));

        if (!clienteRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("Cliente con id " + id + " no encontrado");
        }

        return pedidosClient.obtenerPedidosPorCliente(id);
    }

    /**
     * Método auxiliar que convierte una entidad Cliente a ClienteResponseDTO
     * y evalúa si corresponde enmascarar sus datos personales
     */
    private ClienteResponseDTO mapearYEnmascararSegunFlag(Cliente cliente) {
        ClienteResponseDTO dto = ClienteMapper.toDTO(cliente);
        if (featureFlagServicio.debeEnmascararPii(cliente.getId())) {
            return EnmascaradorPii.enmascarar(dto);
        }
        return dto;
    }

}