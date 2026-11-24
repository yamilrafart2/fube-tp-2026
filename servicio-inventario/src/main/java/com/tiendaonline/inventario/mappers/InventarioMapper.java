package com.tiendaonline.inventario.mappers;

import com.tiendaonline.inventario.dtos.ProductoRequestDTO;
import com.tiendaonline.inventario.dtos.ProductoResponseDTO;
import com.tiendaonline.inventario.dtos.ReservaRequestDTO;
import com.tiendaonline.inventario.dtos.ReservaResponseDTO;
import com.tiendaonline.inventario.models.EstadoReserva;
import com.tiendaonline.inventario.models.Producto;
import com.tiendaonline.inventario.models.Reserva;

import java.math.BigDecimal;

/**
 * Mapper encargado de convertir objetos Producto, Reserva y sus respectivos DTOs
 */
public class InventarioMapper {

    private InventarioMapper() {}

    // Convierte un ProductoRequestDTO den una nueva entidad Producto
    public static Producto toProductoEntity(ProductoRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Producto.builder()
                .sku(dto.getSku())
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .cantidadDisponible(dto.getCantidadDisponible())
                .build();
    }

    // Convierte una entidad Producto en ProductoResponseDTO
    public static ProductoResponseDTO toProductoDTO(Producto producto) {
        if (producto == null) {
            return null;
        }

        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .sku(producto.getSku())
                .nombre(producto.getNombre())
                .precio(producto.getPrecio())
                .cantidadDisponible(producto.getCantidadDisponible())
                .build();
    }

    //Convierte un ReservaRequestDTO en una entidad Reserva
    public static Reserva toReservaEntity(ReservaRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Reserva.builder()
                .productoId(dto.getProductoId())
                .pedidoId(dto.getPedidoId())
                .cantidad(dto.getCantidad())
                .estado(EstadoReserva.ACTIVA)
                .build();
    }

    // Convierte una entidad Reserva en ReservaResponseDTO
    public static ReservaResponseDTO toReservaDTO(Reserva reserva, BigDecimal precioUnitario) {
        if (reserva == null) {
            return null;
        }

        return ReservaResponseDTO.builder()
                .id(reserva.getId())
                .productoId(reserva.getProductoId())
                .pedidoId(reserva.getPedidoId())
                .cantidad(reserva.getCantidad())
                .estado(reserva.getEstado())
                .precioUnitario(precioUnitario)
                .fechaCreacion(reserva.getFechaCreacion())
                .build();
    }

}
