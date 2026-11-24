package com.tiendaonline.pedidos.repositories;

import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.ItemPedido;
import com.tiendaonline.pedidos.models.Pedido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del PedidoRepositorio
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.batch.job.enabled=false",
        "spring.batch.jdbc.initialize-schema=never"
})
public class PedidoRepositorioTest {

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @Test
    void deberiaGuardarPedidoConItemsEnCascadaYRecuperarloPorId() {
        // 1. Arrange
        Pedido nuevo = Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .build();

        ItemPedido item = ItemPedido.builder()
                .productoId(10L)
                .cantidad(2)
                .precioUnitario(BigDecimal.valueOf(45000.00))
                .build();

        nuevo.agregarItem(item);

        // 2. Act
        Pedido guardado = pedidoRepositorio.save(nuevo);
        Optional<Pedido> encontrado = pedidoRepositorio.findById(guardado.getId());

        // 3. Assert
        assertTrue(encontrado.isPresent());
        assertNotNull(encontrado.get().getFechaCreacion());
        assertEquals(1, encontrado.get().getListaItems().size());
        assertNotNull(encontrado.get().getListaItems().getFirst().getId());
    }

    @Test
    void deberiaListarPedidosPorClienteIdYFiltrarPorEstado() {
        // 1. Arrange
        Pedido pedido1 = Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .build();
        pedidoRepositorio.save(pedido1);

        Pedido pedido2 = Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CANCELADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .build();
        pedidoRepositorio.save(pedido2);

        Pedido pedido3 = Pedido.builder()
                .clienteId(2L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .build();
        pedidoRepositorio.save(pedido3);

        // 2. Act
        List<Pedido> todosCliente1 = pedidoRepositorio.findByClienteIdOrderByFechaCreacionDesc(1L);
        List<Pedido> creadosCliente1 = pedidoRepositorio.findByClienteIdAndEstadoOrderByFechaCreacionDesc(
                1L, EstadoPedido.CREADO);

        // 3. Assert
        assertEquals(2, todosCliente1.size());
        assertEquals(1, creadosCliente1.size());
        assertEquals(EstadoPedido.CREADO, creadosCliente1.getFirst().getEstado());
    }

    @Test
    void deberiaBuscarPedidosCreadosConMasDe24HorasDeAntiguedad() {
        // 1. Arrange
        OffsetDateTime hace30Horas = OffsetDateTime.now().minusHours(30);
        OffsetDateTime hace2Horas = OffsetDateTime.now().minusHours(2);

        Pedido pedidoAntiguo = Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .fechaCreacion(hace30Horas)
                .build();
        pedidoRepositorio.save(pedidoAntiguo);

        Pedido pedidoReciente = Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .fechaCreacion(hace2Horas)
                .build();
        pedidoRepositorio.save(pedidoReciente);

        Pedido pedidoAntiguoConfirmado = Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CONFIRMADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .fechaCreacion(hace30Horas)
                .build();
        pedidoRepositorio.save(pedidoAntiguoConfirmado);

        // 2. Act
        OffsetDateTime limite24Horas = OffsetDateTime.now().minusHours(24);
        Page<Pedido> vencidos = pedidoRepositorio.findByEstadoAndFechaCreacionBefore(
                EstadoPedido.CREADO,
                limite24Horas,
                PageRequest.of(0, 10));

        // 3. Assert
        assertEquals(1, vencidos.getTotalElements());
        assertEquals(EstadoPedido.CREADO, vencidos.stream().findFirst().orElseThrow().getEstado());
        assertEquals(hace30Horas, vencidos.stream().findFirst().orElseThrow().getFechaCreacion());
    }

}
