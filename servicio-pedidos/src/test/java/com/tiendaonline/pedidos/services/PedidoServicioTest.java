package com.tiendaonline.pedidos.services;

import com.tiendaonline.pedidos.clients.ClientesClient;
import com.tiendaonline.pedidos.clients.InventarioClient;
import com.tiendaonline.pedidos.clients.dtos.ReservaInventarioResponseDTO;
import com.tiendaonline.pedidos.dtos.ItemPedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoResponseDTO;
import com.tiendaonline.pedidos.exceptions.ConflictoException;
import com.tiendaonline.pedidos.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.Pedido;
import com.tiendaonline.pedidos.repositories.PedidoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas del PedidoServicioImpl
 */
@ExtendWith(MockitoExtension.class)
public class PedidoServicioTest {

    @Mock
    private PedidoRepositorio pedidoRepositorio;

    @Mock
    private ClientesClient clientesClient;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoServicioImpl pedidoServicio;

    @Test
    void deberiaCrearPedidoExitosamenteCalculandoMontoTotal() {
        // 1. Arrange
        ItemPedidoRequestDTO item1 = ItemPedidoRequestDTO.builder().productoId(10L).cantidad(2).build();
        ItemPedidoRequestDTO item2 = ItemPedidoRequestDTO.builder().productoId(11L).cantidad(1).build();
        PedidoRequestDTO request = PedidoRequestDTO.builder()
                .clienteId(1L)
                .listaItems(List.of(item1, item2))
                .build();

        when(pedidoRepositorio.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(501L);
            return p;
        });

        when(inventarioClient.reservarStock(10L, 501L, 2)).thenReturn(
                ReservaInventarioResponseDTO.builder().precioUnitario(BigDecimal.valueOf(45000.00)).build());
        when(inventarioClient.reservarStock(11L, 501L, 1)).thenReturn(
                ReservaInventarioResponseDTO.builder().precioUnitario(BigDecimal.valueOf(20000.00)).build());

        // 2. Act
        PedidoResponseDTO resultado = pedidoServicio.crearPedido(request);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(501L, resultado.getId());
        assertEquals(EstadoPedido.CREADO, resultado.getEstado());
        assertEquals(BigDecimal.valueOf(110000.00), resultado.getMontoTotal());
        assertEquals(2, resultado.getListaItems().size());
        verify(clientesClient).verificarClienteExiste(1L);
    }

    @Test
    void noDeberiaCrearPedidoCuandoClienteNoExiste() {
        // 1. Arrange
        PedidoRequestDTO request = PedidoRequestDTO.builder()
                .clienteId(99L)
                .listaItems(List.of(ItemPedidoRequestDTO.builder().productoId(10L).cantidad(1).build()))
                .build();

        doThrow(new RecursoNoEncontradoException("Cliente no encontrado"))
                .when(clientesClient).verificarClienteExiste(99L);

        // 2. Act & 3. Assert
        assertThrows(RecursoNoEncontradoException.class, () -> pedidoServicio.crearPedido(request));
        verify(pedidoRepositorio, never()).save(any());
        verify(inventarioClient, never()).reservarStock(anyLong(), anyLong(), anyInt());
    }

    @Test
    void deberiaCompensarLiberandoReservasPreviasSiFallaSegundoItem() {
        // 1. Arrange
        ItemPedidoRequestDTO item1 = ItemPedidoRequestDTO.builder().productoId(10L).cantidad(2).build();
        ItemPedidoRequestDTO item2 = ItemPedidoRequestDTO.builder().productoId(11L).cantidad(50).build();
        PedidoRequestDTO request = PedidoRequestDTO.builder()
                .clienteId(1L)
                .listaItems(List.of(item1, item2))
                .build();

        when(pedidoRepositorio.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(501L);
            return p;
        });

        when(inventarioClient.reservarStock(10L, 501L, 2)).thenReturn(
                ReservaInventarioResponseDTO.builder().precioUnitario(BigDecimal.valueOf(45000.00)).build());
        when(inventarioClient.reservarStock(11L, 501L, 50)).thenThrow(
                new ConflictoException("STOCK_INSUFICIENTE", "Stock insuficiente"));

        // 2. Act & 3. Assert
        assertThrows(ConflictoException.class, () -> pedidoServicio.crearPedido(request));
        verify(inventarioClient).liberarReservasPorPedido(501L);
    }

    @Test
    void deberiaConfirmarPedidoCuandoEstaEnEstadoCreado() {
        // 1. Arrange
        Pedido pedido = Pedido.builder()
                .id(501L).clienteId(1L).estado(EstadoPedido.CREADO).montoTotal(BigDecimal.valueOf(45000.00)).build();

        when(pedidoRepositorio.findById(501L)).thenReturn(Optional.of(pedido));
        when(pedidoRepositorio.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2. Act
        PedidoResponseDTO resultado = pedidoServicio.confirmarPedido(501L);

        // 3. Assert
        assertEquals(EstadoPedido.CONFIRMADO, resultado.getEstado());
    }

    @Test
    void deberiaLanzarConflictoExceptionAlConfirmarPedidoQueNoEstaCreado() {
        // 1. Arrange
        Pedido pedidoCancelado = Pedido.builder()
                .id(501L).clienteId(1L).estado(EstadoPedido.CANCELADO).build();

        when(pedidoRepositorio.findById(501L)).thenReturn(Optional.of(pedidoCancelado));

        // 2. Act & 3. Assert
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> pedidoServicio.confirmarPedido(501L));

        assertEquals("TRANSICION_INVALIDA", ex.getCodigoError());
    }

    @Test
    void deberiaCancelarPedidoEnEstadoCreadoYLiberarReservas() {
        // 1. Arrange
        Pedido pedido = Pedido.builder()
                .id(501L).clienteId(1L).estado(EstadoPedido.CREADO).montoTotal(BigDecimal.valueOf(45000.00)).build();

        when(pedidoRepositorio.findById(501L)).thenReturn(Optional.of(pedido));
        when(pedidoRepositorio.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2. Act
        PedidoResponseDTO resultado = pedidoServicio.cancelarPedido(501L);

        // 3. Assert
        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());
        verify(inventarioClient).liberarReservasPorPedido(501L);
    }

    @Test
    void deberiaLanzarConflictoExceptionAlCancelarPedidoYaExpirado() {
        // 1. Arrange
        Pedido pedidoExpirado = Pedido.builder()
                .id(501L).clienteId(1L).estado(EstadoPedido.EXPIRADO).build();

        when(pedidoRepositorio.findById(501L)).thenReturn(Optional.of(pedidoExpirado));

        // 2. Act & 3. Assert
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> pedidoServicio.cancelarPedido(501L));

        assertEquals("TRANSICION_INVALIDA", ex.getCodigoError());
        verify(inventarioClient, never()).liberarReservasPorPedido(anyLong());
    }
}