package com.tiendaonline.pedidos.dtos;

import com.tiendaonline.pedidos.models.EstadoPedido;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO de salida que representa un pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;

    private Long clienteId;

    private EstadoPedido estado;

    private BigDecimal montoTotal;

    private OffsetDateTime fechaCreacion;

    private OffsetDateTime fechaActualizacion;

    private List<ItemPedidoResponseDTO> listaItems;

}
