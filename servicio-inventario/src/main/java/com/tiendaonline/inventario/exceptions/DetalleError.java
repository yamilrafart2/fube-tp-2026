package com.tiendaonline.inventario.exceptions;

import lombok.*;

/**
 * Representa el detalle de un error de validación sobre un campo específico
 * dentro del cuerpo de una petición HTTP
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleError {

    private String campo;

    private String mensaje;

}
