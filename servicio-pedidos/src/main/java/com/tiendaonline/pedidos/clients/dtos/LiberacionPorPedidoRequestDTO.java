package com.tiendaonline.pedidos.clients.dtos;

import lombok.*;

/**
 * DTO para liberar las reservas activas de un pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiberacionPorPedidoRequestDTO {

    private Long pedidoId;

}