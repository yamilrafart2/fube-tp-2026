package com.tiendaonline.inventario.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO de entrada para solicitar la liberación masiva de todas las reservas activas
 * asociadas a un pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiberacionPorPedidoRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser un número positivo")
    private Long pedidoId;

}
