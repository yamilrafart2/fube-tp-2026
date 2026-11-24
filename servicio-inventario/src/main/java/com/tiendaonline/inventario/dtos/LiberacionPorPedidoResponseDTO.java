package com.tiendaonline.inventario.dtos;

import lombok.*;

/**
 * DTO de salida que informa el resultado de la liberación masiva de reservas de un pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiberacionPorPedidoResponseDTO {

    private Long pedidoId;

    private Integer reservasLiberadas;

}
