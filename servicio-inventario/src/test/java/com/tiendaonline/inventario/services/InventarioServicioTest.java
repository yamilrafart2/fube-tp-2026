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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas del InventarioServicioImpl
 */
@ExtendWith(MockitoExtension.class)
public class InventarioServicioTest {

    @Mock
    private ProductoRepositorio productoRepositorio;

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @InjectMocks
    private InventarioServicioImpl inventarioServicio;

    @Test
    void deberiaCrearProductoExitosamenteCuandoSkuNoExiste() {
        // 1. Arrange
        ProductoRequestDTO request = ProductoRequestDTO.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        Producto guardado = Producto.builder()
                .id(1L)
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        when(productoRepositorio.save(any(Producto.class))).thenReturn(guardado);

        // 2. Act
        ProductoResponseDTO resultado = inventarioServicio.crearProducto(request);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("TECL-001", resultado.getSku());
        assertEquals("Teclado Mecánico", resultado.getNombre());
        assertEquals(BigDecimal.valueOf(45000.00), resultado.getPrecio());
        assertEquals(10, resultado.getCantidadDisponible());
        verify(productoRepositorio).save(any(Producto.class));
    }

    @Test
    void deberiaLanzarConflictoExceptionCuandoSkuYaExiste() {
        // 1. Arrange
        ProductoRequestDTO request = ProductoRequestDTO.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        when(productoRepositorio.existsBySku("TECL-001")).thenReturn(true);

        // 2. Act & 3. Assert
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> inventarioServicio.crearProducto(request));

        assertEquals("SKU_DUPLICADO", ex.getCodigoError());
        verify(productoRepositorio, never()).save(any());
    }

    @Test
    void deberiaObtenerProductoPorIdCuandoExiste() {
        // 1. Arrange
        Producto producto = Producto.builder()
                .id(1L)
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        when(productoRepositorio.findById(1L)).thenReturn(Optional.of(producto));

        // 2. Act
        ProductoResponseDTO resultado = inventarioServicio.obtenerProductoPorId(1L);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("TECL-001", resultado.getSku());
        assertEquals("Teclado Mecánico", resultado.getNombre());
        assertEquals(BigDecimal.valueOf(45000.00), resultado.getPrecio());
        assertEquals(10, resultado.getCantidadDisponible());
    }

    @Test
    void deberiaLanzarRecursoNoEncontradoExceptionCuandoProductoNoExiste() {
        // 1. Arrange
        when(productoRepositorio.findById(99L)).thenReturn(Optional.empty());

        // 2. Act & 3. Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> inventarioServicio.obtenerProductoPorId(99L));
    }

    @Test
    void deberiaReservarStockExitosamenteYRetornarPrecioUnitario() {
        // 1. Arrange
        Producto producto = Producto.builder()
                .id(1L)
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        when(productoRepositorio.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepositorio.descontarStock(1L, 2)).thenReturn(1);

        ReservaRequestDTO reservaRequest = ReservaRequestDTO.builder()
                .productoId(1L)
                .pedidoId(501L)
                .cantidad(2)
                .build();

        Reserva reservaGuardada = Reserva.builder()
                .id(77L)
                .productoId(1L)
                .pedidoId(501L)
                .cantidad(2)
                .estado(EstadoReserva.ACTIVA)
                .fechaCreacion(OffsetDateTime.now())
                .build();

        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reservaGuardada);

        // 2. Act
        ReservaResponseDTO reservaResponse = inventarioServicio.reservarStock(reservaRequest);

        // 3. Assert
        assertEquals(EstadoReserva.ACTIVA, reservaResponse.getEstado());
        assertEquals(BigDecimal.valueOf(45000.00), reservaResponse.getPrecioUnitario());
        verify(reservaRepositorio).save(any(Reserva.class));
    }

    @Test
    void deberiaLanzarConflictoExceptionAlReservarCuandoStockEsInsuficiente() {
        // 1. Arrange
        Producto producto = Producto.builder()
                .id(1L)
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(50)
                .build();

        when(productoRepositorio.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepositorio.descontarStock(1L, 50)).thenReturn(0);

        ReservaRequestDTO reservaRequest = ReservaRequestDTO.builder()
                .productoId(1L)
                .pedidoId(501L)
                .cantidad(50)
                .build();

        // 2. Act & 3. Assert
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> inventarioServicio.reservarStock(reservaRequest));

        assertEquals("STOCK_INSUFICIENTE", ex.getCodigoError());
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void deberiaLiberarReservaActivaYRestituirStock() {
        // 1. Arrange
        Reserva reservaActiva = Reserva.builder()
                .id(77L)
                .productoId(1L)
                .pedidoId(100L)
                .cantidad(10)
                .estado(EstadoReserva.ACTIVA)
                .fechaCreacion(OffsetDateTime.now())
                .build();

        when(reservaRepositorio.findById(77L)).thenReturn(Optional.of(reservaActiva));
        when(reservaRepositorio.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        // 2. Act
        ReservaResponseDTO resultado = inventarioServicio.liberarReserva(77L);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(EstadoReserva.LIBERADA, resultado.getEstado());
        verify(reservaRepositorio).save(reservaActiva);
        verify(productoRepositorio).incrementarStock(1L, 10);
    }

    @Test
    void noDeberiaRestituirStockAlLiberarReservaYaLiberada() {
        // 1. Arrange
        Reserva reservaYaLiberada = Reserva.builder()
                .id(77L)
                .productoId(1L)
                .pedidoId(100L)
                .cantidad(10)
                .estado(EstadoReserva.LIBERADA)
                .fechaCreacion(OffsetDateTime.now())
                .build();

        when(reservaRepositorio.findById(77L)).thenReturn(Optional.of(reservaYaLiberada));

        // 2. Act
        ReservaResponseDTO resultado = inventarioServicio.liberarReserva(77L);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(EstadoReserva.LIBERADA, resultado.getEstado());
        verify(productoRepositorio, never()).incrementarStock(anyLong(), anyInt());
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void deberiaLiberarMasivamenteReservasActivasDeUnPedido() {
        // 1. Arrange
        Reserva r1 = Reserva.builder()
                .id(77L).productoId(10L).pedidoId(501L).cantidad(2).estado(EstadoReserva.ACTIVA).build();
        Reserva r2 = Reserva.builder()
                .id(78L).productoId(11L).pedidoId(501L).cantidad(3).estado(EstadoReserva.ACTIVA).build();

        when(reservaRepositorio.findByPedidoIdAndEstado(501L, EstadoReserva.ACTIVA))
                .thenReturn(List.of(r1, r2));

        LiberacionPorPedidoRequestDTO request = LiberacionPorPedidoRequestDTO.builder()
                .pedidoId(501L)
                .build();

        // 2. Act
        LiberacionPorPedidoResponseDTO resultado = inventarioServicio.liberarReservasPorPedido(request);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(501L, resultado.getPedidoId());
        assertEquals(2, resultado.getReservasLiberadas());
        assertEquals(EstadoReserva.LIBERADA, r1.getEstado());
        assertEquals(EstadoReserva.LIBERADA, r2.getEstado());
        verify(productoRepositorio).incrementarStock(10L, 2);
        verify(productoRepositorio).incrementarStock(11L, 3);
    }
}
