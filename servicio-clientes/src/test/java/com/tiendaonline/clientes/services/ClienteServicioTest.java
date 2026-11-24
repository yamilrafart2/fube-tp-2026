package com.tiendaonline.clientes.services;

import com.tiendaonline.clientes.clients.PedidosClient;
import com.tiendaonline.clientes.clients.dtos.ItemPedidoClienteResponseDTO;
import com.tiendaonline.clientes.clients.dtos.PedidoClienteResponseDTO;
import com.tiendaonline.clientes.dtos.ClienteRequestDTO;
import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import com.tiendaonline.clientes.exceptions.ConflictoException;
import com.tiendaonline.clientes.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.clientes.models.Cliente;
import com.tiendaonline.clientes.repositories.ClienteRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas del ClienteServicioImpl
 */
@ExtendWith(MockitoExtension.class)
public class ClienteServicioTest {

    @Mock
    private ClienteRepositorio clienteRepositorio;

    @Mock
    private FeatureFlagServicio featureFlagServicio;

    @Mock
    private PedidosClient pedidosClient;

    @InjectMocks
    private ClienteServicioImpl clienteServicio;

    @Test
    void deberiaCrearClienteSinEnmascararCuandoFlagEstaInactivo() {
        // 1. Arrange
        ClienteRequestDTO request = ClienteRequestDTO.builder()
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .build();

        Cliente guardado = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .creadoEn(OffsetDateTime.now())
                .build();

        when(clienteRepositorio.existsByEmail("juan@email.com")).thenReturn(false);
        when(clienteRepositorio.save(any(Cliente.class))).thenReturn(guardado);
        when(featureFlagServicio.debeEnmascararPii(1L)).thenReturn(false);

        // 2. Act
        ClienteResponseDTO respuesta = clienteServicio.crearCliente(request);

        // 3. Assert
        assertNotNull(respuesta);
        assertEquals(1L, respuesta.getId());
        assertEquals("Juan Pérez", respuesta.getNombre());
        assertEquals("juan@email.com", respuesta.getEmail());
        verify(clienteRepositorio).save(any(Cliente.class));
    }

    @Test
    void deberiaLanzarConflictoExceptionCuandoEmailYaExisteAlCrear() {
        // 1. Arrange
        ClienteRequestDTO request = ClienteRequestDTO.builder()
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .build();

        when(clienteRepositorio.existsByEmail("juan@email.com")).thenReturn(true);

        // 2. Act & 3. Assert
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> clienteServicio.crearCliente(request));

        assertEquals("EMAIL_DUPLICADO", ex.getCodigoError());
        verify(clienteRepositorio, never()).save(any(Cliente.class));
    }

    @Test
    void deberiaObtenerClientePorIdSinEnmascararCuandoFlagEstaInactivo() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .build();

        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(featureFlagServicio.debeEnmascararPii(1L)).thenReturn(false);

        ClienteResponseDTO respuesta = clienteServicio.obtenerClientePorId(1L);

        assertNotNull(respuesta);
        assertEquals(1L, respuesta.getId());
        assertEquals("Juan Pérez", respuesta.getNombre());
        assertEquals("juan@email.com", respuesta.getEmail());
    }

    @Test
    void deberiaObtenerClientePorIdEnmascaradoCuandoFlagEstaActivo() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .build();

        when(clienteRepositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(featureFlagServicio.debeEnmascararPii(1L)).thenReturn(true);

        ClienteResponseDTO respuesta = clienteServicio.obtenerClientePorId(1L);

        assertEquals("J*** P***", respuesta.getNombre());
        assertEquals("***@e***.com", respuesta.getEmail());
        assertEquals("***4567", respuesta.getTelefono());
        assertEquals("***", respuesta.getDireccion());
    }

    @Test
    void deberiaLanzarRecursoNoEncontradoExceptionCuandoClienteNoExiste() {
        when(clienteRepositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> clienteServicio.obtenerClientePorId(99L));
    }

    @Test
    void deberiaListarClientesPaginadosAplicandoFlagMaskPii() {
        Pageable pageable = PageRequest.of(0, 10);
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .build();
        Page<Cliente> paginaSimulada = new PageImpl<>(List.of(cliente));
        when(clienteRepositorio.findAll(pageable)).thenReturn(paginaSimulada);
        when(featureFlagServicio.debeEnmascararPii(1L)).thenReturn(true);

        Page<ClienteResponseDTO> resultado = clienteServicio.listarClientes(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("J*** P***", resultado.getContent().getFirst().getNombre());
    }

    @Test
    void deberiaObtenerPedidosDeClienteCuandoClienteExiste() {
        // 1. Arrange
        PedidoClienteResponseDTO pedidoDto = PedidoClienteResponseDTO.builder()
                .id(501L)
                .clienteId(1L)
                .estado("CREADO")
                .montoTotal(BigDecimal.valueOf(90000.00))
                .build();

        when(clienteRepositorio.existsById(1L)).thenReturn(true);
        when(pedidosClient.obtenerPedidosPorCliente(1L)).thenReturn(List.of(pedidoDto));

        // 2. Act
        List<PedidoClienteResponseDTO> listaPedidos = clienteServicio.obtenerPedidosDeCliente(1L);

        // 3. Assert
        assertEquals(1, listaPedidos.size());
        assertEquals(501L, listaPedidos.getFirst().getId());
    }

    @Test
    void deberiaLanzarRecursoNoEncontradoAlConsultarPedidosSiClienteNoExiste() {
        // 1. Arrange
        when(clienteRepositorio.existsById(99L)).thenReturn(false);

        // 2. Act & 3. Assert
        assertThrows(RecursoNoEncontradoException.class, () -> clienteServicio.obtenerPedidosDeCliente(99L));
        verify(pedidosClient, never()).obtenerPedidosPorCliente(anyLong());
    }

}