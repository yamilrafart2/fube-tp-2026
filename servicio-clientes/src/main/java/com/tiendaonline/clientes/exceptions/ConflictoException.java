package com.tiendaonline.clientes.exceptions;

import lombok.Getter;

/**
 * Excepción que se lanza cuando una operación válida viola una regla de negocio
 */
@Getter
public class ConflictoException extends RuntimeException {


    // Código que identifica la regla de negocio en conflicto
    // ("EMAIL_DUPLICADO", "STOCK_INSUFICIENTE", "TRANSICION_INVALIDA")
    private final String codigoError;

    public ConflictoException(String codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public ConflictoException(String mensaje) {
        this("CONFLICTO", mensaje);
    }

}
