package com.tiendaonline.inventario.dtos;

import java.math.BigDecimal;
import lombok.*;

/**
 * DTO de salida con la información del producto
 *.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Long id;

    private String sku;

    private String nombre;

    private BigDecimal precio;

    private Integer cantidadDisponible;

}
