package com.tiendaonline.inventario.exceptions;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Estructura para todas las respuestas HTTP de error
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    // Código numérico del estado HTTP (400, 404, 409, 503)
    private int status;

    // Código semántico estable del error en mayúsculas ("RECURSO_NO_ENCONTRADO", "STOCK_INSUFICIENTE")
    private String error;

    // Mensaje descriptivo
    private String mensaje;

    // Lista de errores por campo
    private List<DetalleError> detalles;

    // Fecha y hora en la que se produjo el error
    private OffsetDateTime timestamp;

    // Ruta del endpoint donde ocurrió el error
    private String path;

}
