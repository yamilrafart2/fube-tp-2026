package com.tiendaonline.clientes.clients.dtos;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que representa cada ítem de un pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoClienteResponseDTO {

    private Long id;

    private Long productoId;

    private Integer cantidad;

    private BigDecimal precioUnitario;

}
