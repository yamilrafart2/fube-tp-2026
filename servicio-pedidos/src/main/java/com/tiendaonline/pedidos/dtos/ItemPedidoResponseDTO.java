package com.tiendaonline.pedidos.dtos;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO de salida que representa el detalle de un ítem de un pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoResponseDTO {

    private Long id;

    private Long productoId;

    private Integer cantidad;

    private BigDecimal precioUnitario;

}
