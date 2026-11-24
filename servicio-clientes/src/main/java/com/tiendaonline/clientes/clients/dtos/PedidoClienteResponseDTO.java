package com.tiendaonline.clientes.clients.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO que representa un pedido de un cliente
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoClienteResponseDTO {

    private Long id;

    private Long clienteId;

    //CREADO CONFIRMADO CANCELADO EXPIRADO
    private String estado;

    private BigDecimal montoTotal;

    private OffsetDateTime fechaCreacion;

    private OffsetDateTime fechaActualizacion;

    private List<ItemPedidoClienteResponseDTO> listaItems;

}
