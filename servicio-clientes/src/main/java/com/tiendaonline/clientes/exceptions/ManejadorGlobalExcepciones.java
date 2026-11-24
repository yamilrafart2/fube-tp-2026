package com.tiendaonline.clientes.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Manejador global de excepciones para controlador REST de servicio-clientes
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    /**
     * Captura RecursoNoEncontradoException cuando un recurso buscado no existe
     * y devuelve HTTP 404 Not Found
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        ErrorResponse respuesta = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("RECURSO_NO_ENCONTRADO")
                .mensaje(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    /**
     * Captura ConflictoException cuando se viola una regla de negocio
     * y devuelve HTTP 409 Conflict
     */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> manejarConflicto(
            ConflictoException ex,
            HttpServletRequest request
    ) {
        ErrorResponse respuesta = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(ex.getCodigoError())
                .mensaje(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    /**
     * Captura ServicioNoDisponibleException cuando falla la comunicación con otro
     * microservicio y devuelve HTTP 503 Service Unavailable
     */
    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> manejarServicioNoDisponible(
            ServicioNoDisponibleException ex,
            HttpServletRequest request
    ) {
        ErrorResponse respuesta = ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("SERVICIO_NO_DISPONIBLE")
                .mensaje(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(respuesta);
    }

    /**
     * Captura MethodArgumentNotValidException cuando falla la validación automática
     * (@Valid) de un DTO de entrada y devuelve HTTP 400 Bad Request incluyendo
     * la lista completa de todos los campos que fallaron
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        // Recorre todos los errores de campos detectados por Bean Validation y los transforma a DetalleError
        List<DetalleError> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new DetalleError(err.getField(), err.getDefaultMessage()))
                .toList();

        ErrorResponse respuesta = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDACION")
                .mensaje("Error de validación en los datos de entrada")
                .detalles(detalles)
                .timestamp(OffsetDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

}
