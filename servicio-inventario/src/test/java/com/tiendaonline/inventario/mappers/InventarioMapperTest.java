package com.tiendaonline.inventario.mappers;

import com.tiendaonline.inventario.dtos.ProductoRequestDTO;
import com.tiendaonline.inventario.dtos.ProductoResponseDTO;
import com.tiendaonline.inventario.dtos.ReservaRequestDTO;
import com.tiendaonline.inventario.dtos.ReservaResponseDTO;
import com.tiendaonline.inventario.models.EstadoReserva;
import com.tiendaonline.inventario.models.Producto;
import com.tiendaonline.inventario.models.Reserva;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para InventarioMapper
 */
public class InventarioMapperTest {

    @Test
    void deberiaConvertirProductoRequestDtoAEntidad() {
        // 1. Arrange:
        ProductoRequestDTO dto = ProductoRequestDTO.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(25)
                .build();

        // 2. Act:
        Producto entidad = InventarioMapper.toProductoEntity(dto);

        // 3. Assert:
        assertNotNull(entidad);
        assertNull(entidad.getId());
        assertEquals("TECL-001", entidad.getSku());
        assertEquals("Teclado Mecánico", entidad.getNombre());
        assertEquals(BigDecimal.valueOf(45000.00), entidad.getPrecio());
        assertEquals(25, entidad.getCantidadDisponible());
    }

    @Test
    void deberiaConvertirProductoEntidadAResponseDto() {
        // 1. Arrange:
        Producto entidad = Producto.builder()
                .id(10L)
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(25)
                .build();

        // 2. Act:
        ProductoResponseDTO dto = InventarioMapper.toProductoDTO(entidad);

        // 3. Assert:
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("TECL-001", dto.getSku());
        assertEquals("Teclado Mecánico", dto.getNombre());
        assertEquals(BigDecimal.valueOf(45000.00), dto.getPrecio());
        assertEquals(25, dto.getCantidadDisponible());
    }

    @Test
    void deberiaConvertirReservaRequestDtoAEntidadConEstadoActiva() {
        // 1. Arrange:
        ReservaRequestDTO dto = ReservaRequestDTO.builder()
                .productoId(10L)
                .pedidoId(501L)
                .cantidad(2)
                .build();

        // 2. Act:
        Reserva entidad = InventarioMapper.toReservaEntity(dto);

        // 3. Assert:
        assertNotNull(entidad);
        assertNull(entidad.getId());
        assertNull(entidad.getFechaCreacion());
        assertEquals(10L, entidad.getProductoId());
        assertEquals(501L, entidad.getPedidoId());
        assertEquals(2, entidad.getCantidad());
        assertEquals(EstadoReserva.ACTIVA, entidad.getEstado());
    }

    @Test
    void deberiaConvertirReservaEntidadAReservaResponseDtoConPrecioUnitario() {
        // 1. Arrange:
        OffsetDateTime ahora = OffsetDateTime.now();
        BigDecimal precioUnitario = BigDecimal.valueOf(45000.00);

        Reserva entidad = Reserva.builder()
                .id(77L)
                .productoId(10L)
                .pedidoId(501L)
                .cantidad(2)
                .estado(EstadoReserva.ACTIVA)
                .fechaCreacion(ahora)
                .build();

        // 2. Act:
        ReservaResponseDTO dto = InventarioMapper.toReservaDTO(entidad, precioUnitario);

        // 3. Assert:
        assertNotNull(dto);
        assertEquals(77L, dto.getId());
        assertEquals(10L, dto.getProductoId());
        assertEquals(501L, dto.getPedidoId());
        assertEquals(2, dto.getCantidad());
        assertEquals(EstadoReserva.ACTIVA, dto.getEstado());
        assertEquals(precioUnitario, dto.getPrecioUnitario());
        assertEquals(ahora, dto.getFechaCreacion());
    }

    @Test
    void deberiaRetornarNullCuandoLaEntradaEsNula() {
        assertNull(InventarioMapper.toProductoEntity(null));
        assertNull(InventarioMapper.toProductoDTO(null));
        assertNull(InventarioMapper.toReservaEntity(null));
        assertNull(InventarioMapper.toReservaDTO(null, new BigDecimal("100.00")));
    }
}