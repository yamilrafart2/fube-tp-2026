package com.tiendaonline.inventario.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import lombok.*;


/**
 * DTO de entrada para la creación de un nuevo producto
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 64, message = "El SKU no puede superar los 64 caracteres")
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private BigDecimal precio;

    @NotNull(message = "La cantidad disponible es obligatoria")
    @PositiveOrZero(message = "La cantidad disponible no puede ser negativa")
    private Integer cantidadDisponible;

}
