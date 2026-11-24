package com.tiendaonline.pedidos.mappers;

import com.tiendaonline.pedidos.dtos.ItemPedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.ItemPedidoResponseDTO;
import com.tiendaonline.pedidos.dtos.PedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoResponseDTO;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.ItemPedido;
import com.tiendaonline.pedidos.models.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para PedidoMapper
 */
public class PedidoMapperTest {

    @Test
    void deberiaConvertirItemPedidoEntidadAResponseDto() {
        // 1. Arrange
        ItemPedido entidad = ItemPedido.builder()
                .id(1L)
                .productoId(10L)
                .cantidad(2)
                .precioUnitario(BigDecimal.valueOf(45000.00))
                .build();

        // 2. Act
        ItemPedidoResponseDTO dto = PedidoMapper.toItemDTO(entidad);

        // 3. Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getProductoId());
        assertEquals(2, dto.getCantidad());
        assertEquals(BigDecimal.valueOf(45000.00), dto.getPrecioUnitario());
    }

    @Test
    void deberiaConvertirPedidoEntidadAResponseDtoConSusItems() {
        // 1. Arrange
        OffsetDateTime ahora = OffsetDateTime.now();

        Pedido entidad = Pedido.builder()
                .id(501L)
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .fechaCreacion(ahora)
                .fechaActualizacion(ahora)
                .build();

        ItemPedido item = ItemPedido.builder()
                .id(1L)
                .productoId(10L)
                .cantidad(2)
                .precioUnitario(BigDecimal.valueOf(45000.00))
                .build();

        entidad.agregarItem(item);

        // 2. Act
        PedidoResponseDTO dto = PedidoMapper.toPedidoDTO(entidad);

        // 3. Assert
        assertNotNull(dto);
        assertEquals(501L, dto.getId());
        assertEquals(1L, dto.getClienteId());
        assertEquals(EstadoPedido.CREADO, dto.getEstado());
        assertEquals(BigDecimal.valueOf(90000.00), dto.getMontoTotal());
        assertEquals(ahora, dto.getFechaCreacion());
        assertEquals(ahora, dto.getFechaActualizacion());
        assertEquals(1, dto.getListaItems().size());
        assertEquals(10L, dto.getListaItems().getFirst().getProductoId());
        assertEquals(BigDecimal.valueOf(45000.00), dto.getListaItems().getFirst().getPrecioUnitario());
    }

    @Test
    void deberiaConvertirItemPedidoRequestDtoAEntidadConPrecioUnitario() {
        // 1. Arrange
        ItemPedidoRequestDTO dto = ItemPedidoRequestDTO.builder()
                .productoId(10L)
                .cantidad(2)
                .build();

        // 2. Act
        ItemPedido entidad = PedidoMapper.toItemEntity(dto, BigDecimal.valueOf(45000.00));

        // 3. Assert
        assertNotNull(entidad);
        assertNull(entidad.getId());
        assertEquals(10L, entidad.getProductoId());
        assertEquals(2, entidad.getCantidad());
        assertEquals(BigDecimal.valueOf(45000.00), entidad.getPrecioUnitario());
    }

    @Test
    void deberiaConvertirPedidoRequestDtoAEntidadConEstadoCreado() {
        // 1. Arrange
        PedidoRequestDTO dto = PedidoRequestDTO.builder()
                .clienteId(1L)
                .listaItems(List.of())
                .build();

        // 2. Act
        Pedido entidad = PedidoMapper.toPedidoEntity(dto);

        // 3. Assert
        assertNotNull(entidad);
        assertNull(entidad.getId());
        assertEquals(1L, entidad.getClienteId());
        assertEquals(EstadoPedido.CREADO, entidad.getEstado());
        assertEquals(BigDecimal.ZERO, entidad.getMontoTotal());
    }

    @Test
    void deberiaRetornarNullCuandoLaEntradaEsNula() {
        assertNull(PedidoMapper.toItemEntity(null, BigDecimal.valueOf(100.00)));
        assertNull(PedidoMapper.toPedidoEntity(null));
        assertNull(PedidoMapper.toItemDTO(null));
        assertNull(PedidoMapper.toPedidoDTO(null));
    }

}
