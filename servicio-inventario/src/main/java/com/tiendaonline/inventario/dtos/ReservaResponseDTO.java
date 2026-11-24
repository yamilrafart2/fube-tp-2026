package com.tiendaonline.inventario.dtos;

import com.tiendaonline.inventario.models.EstadoReserva;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;

/**
 * DTO de salida que representa el estado de una reserva e incluye el precio
 * unitario del producto al momento de reservar
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {

    private Long id;

    private Long productoId;

    private Long pedidoId;

    private Integer cantidad;

    private EstadoReserva estado;

    private BigDecimal precioUnitario;

    private OffsetDateTime fechaCreacion;

}
