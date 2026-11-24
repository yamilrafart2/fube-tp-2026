package com.tiendaonline.pedidos.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

/**
 * DTO de entrada para la creación de un nuevo pedido
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser un número positivo")
    private Long clienteId;

    @NotEmpty(message = "El pedido debe contener al menos un ítem")
    @Valid
    private List<ItemPedidoRequestDTO> listaItems;

}
