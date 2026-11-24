package com.tiendaonline.clientes.exceptions;

/**
 * Excepción que se lanza cuando se intenta consultar o manipular un recurso
 * que no existe en la base de datos
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

}
