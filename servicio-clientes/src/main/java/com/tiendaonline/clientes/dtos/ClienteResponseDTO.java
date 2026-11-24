package com.tiendaonline.clientes.dtos;

import lombok.*;

import java.time.OffsetDateTime;

/**
 * DTO de salida (Response) que representa la información de un cliente devuelta por la API
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {

    private Long id;

    private String nombre;

    private String email;

    private String telefono;

    private String direccion;

    private OffsetDateTime creadoEn;

}
