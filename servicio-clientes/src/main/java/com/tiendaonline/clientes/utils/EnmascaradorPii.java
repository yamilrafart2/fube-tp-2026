package com.tiendaonline.clientes.utils;

import com.tiendaonline.clientes.dtos.ClienteResponseDTO;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utilidad encargada de enmascarar los datos personales de un cliente cuando el flag
 * mask-pii de Statsig se encuentra activo
 */
public class EnmascaradorPii {

    public static ClienteResponseDTO enmascarar(ClienteResponseDTO dto) {
        if (dto == null) {
            return null;
        }
        return ClienteResponseDTO.builder()
                .id(dto.getId())
                .nombre(enmascararNombre(dto.getNombre()))
                .email(enmascararEmail(dto.getEmail()))
                .telefono(enmascararTelefono(dto.getTelefono()))
                .direccion(enmascararDireccion(dto.getDireccion()))
                .creadoEn(dto.getCreadoEn())
                .build();
    }

    public static String enmascararNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return nombre;
        }
        return Arrays.stream(nombre.trim().split("\\s+"))
                .map(palabra -> palabra.charAt(0) + "***")
                .collect(Collectors.joining(" "));
    }

    public static String enmascararEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }

        int indiceArroba = email.indexOf('@');
        int indicePunto = email.lastIndexOf('.');

        if (indiceArroba < 0 || indicePunto <= indiceArroba + 1) {
            return "***";
        }

        char primeraLetraDominio = email.charAt(indiceArroba + 1);
        String extension = email.substring(indicePunto);
        return "***@" + primeraLetraDominio + "***" + extension;
    }

    public static String enmascararTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return telefono;
        }

        if (telefono.length() <= 4) {
            return "***";
        }

        return "***" + telefono.substring(telefono.length() - 4);
    }

    public static String enmascararDireccion(String direccion) {
        if (direccion == null || direccion.isBlank()) {
            return direccion;
        }

        return "***";
    }

}
