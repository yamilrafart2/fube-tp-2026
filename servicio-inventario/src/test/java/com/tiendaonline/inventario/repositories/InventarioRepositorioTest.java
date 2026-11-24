package com.tiendaonline.inventario.repositories;

import com.tiendaonline.inventario.models.EstadoReserva;
import com.tiendaonline.inventario.models.Producto;
import com.tiendaonline.inventario.models.Reserva;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de ProductoRepositorio
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class InventarioRepositorioTest {

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private ReservaRepositorio reservaRepositorio;

    @Test
    void deberiaGuardarProductoYVerificarExistenciaPorSku() {
        // 1. Arrange
        Producto nuevo = Producto.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        // 2. Act
        Producto guardado = productoRepositorio.save(nuevo);

        // 3. Assert
        assertNotNull(guardado.getId());
        assertTrue(productoRepositorio.existsBySku(guardado.getSku()));
        assertFalse(productoRepositorio.existsBySku("INEXISTENTE"));
    }

    @Test
    void deberiaDescontarStockAtomicamenteCuandoHayDisponibilidadSuficiente() {
        // 1. Arrange
        Producto nuevo = Producto.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        Producto guardado = productoRepositorio.save(nuevo);

        // 2. Act
        int filasActualizadas = productoRepositorio.descontarStock(guardado.getId(), 4);

        // 3. Assert
        Producto encontrado = productoRepositorio.findById(guardado.getId()).orElseThrow();
        assertEquals(1, filasActualizadas);
        assertEquals(6, encontrado.getCantidadDisponible());
    }

    @Test
    void noDeberiaDescontarStockCuandoLaCantidadSuperaLoDisponible() {
        // 1. Arrange
        Producto nuevo = Producto.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(3)
                .build();

        Producto guardado = productoRepositorio.save(nuevo);

        // 2. Act
        int filasActualizadas = productoRepositorio.descontarStock(guardado.getId(), 5);

        // 3. Assert
        Producto encontrado = productoRepositorio.findById(guardado.getId()).orElseThrow();
        assertEquals(0, filasActualizadas);
        assertEquals(3, encontrado.getCantidadDisponible());
    }

    @Test
    void deberiaIncrementarStockAtomicamenteAlLiberar() {
        // 1. Arrange
        Producto nuevo = Producto.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(6)
                .build();

        Producto guardado = productoRepositorio.save(nuevo);

        // 2. Act
        int filasActualizadas = productoRepositorio.incrementarStock(guardado.getId(), 4);

        // 3. Assert
        Producto encontrado = productoRepositorio.findById(guardado.getId()).orElseThrow();
        assertEquals(1, filasActualizadas);
        assertEquals(10, encontrado.getCantidadDisponible());
    }

    @Test
    void deberiaBuscarReservasPorPedidoIdYEstado() {
        // 1. Arrange
        Producto nuevo = Producto.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        Producto guardado = productoRepositorio.save(nuevo);

        Reserva reserva1 = Reserva.builder()
                .productoId(guardado.getId())
                .pedidoId(501L)
                .cantidad(2)
                .estado(EstadoReserva.ACTIVA)
                .build();
        reservaRepositorio.save(reserva1);

        Reserva reserva2 = Reserva.builder()
                .productoId(guardado.getId())
                .pedidoId(501L)
                .cantidad(1)
                .estado(EstadoReserva.LIBERADA)
                .build();
        reservaRepositorio.save(reserva2);

        Reserva reserva3 = Reserva.builder()
                .productoId(guardado.getId())
                .pedidoId(999L)
                .cantidad(3)
                .estado(EstadoReserva.ACTIVA)
                .build();
        reservaRepositorio.save(reserva3);

        // 2. Act
        List<Reserva> activas = reservaRepositorio.findByPedidoIdAndEstado(501L, EstadoReserva.ACTIVA);

        // 3. Assert
        assertEquals(1, activas.size());
        assertEquals(2, activas.getFirst().getCantidad());
        assertEquals(EstadoReserva.ACTIVA, activas.getFirst().getEstado());
        assertNotNull(activas.getFirst().getFechaCreacion());
    }

}
