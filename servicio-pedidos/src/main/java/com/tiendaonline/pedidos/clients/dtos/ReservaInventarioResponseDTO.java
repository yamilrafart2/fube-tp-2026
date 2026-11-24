package com.tiendaonline.pedidos.clients.dtos;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO de confirmacion de una reserva de stock
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaInventarioResponseDTO {

    private Long id;

    private Long productoId;

    private Long pedidoId;

    private Integer cantidad;

    private String estado;

    private BigDecimal precioUnitario;

}