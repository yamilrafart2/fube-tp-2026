package com.tiendaonline.inventario.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;


/**
 * DTO de entrada para solicitar una reserva de stock de un producto asociada a un pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser un número positivo")
    private Long productoId;

    @NotNull(message = "El ID del pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser un número positivo")
    private Long pedidoId;

    @NotNull(message = "La cantidad a reservar es obligatoria")
    @Positive(message = "La cantidad a reservar debe ser mayor a cero")
    private Integer cantidad;

}
