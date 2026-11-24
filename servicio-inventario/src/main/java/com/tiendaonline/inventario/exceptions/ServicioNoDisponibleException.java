package com.tiendaonline.inventario.exceptions;

/**
 * Excepción que se lanza cuando falla la comunicación HTTP hacia otro microservicio
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

}
