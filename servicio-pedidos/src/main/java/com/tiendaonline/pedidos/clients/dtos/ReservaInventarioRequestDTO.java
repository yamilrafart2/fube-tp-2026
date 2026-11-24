package com.tiendaonline.pedidos.clients.dtos;

import lombok.*;

/**
 * DTO para reservar stock
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaInventarioRequestDTO {

    private Long productoId;

    private Long pedidoId;

    private Integer cantidad;

}