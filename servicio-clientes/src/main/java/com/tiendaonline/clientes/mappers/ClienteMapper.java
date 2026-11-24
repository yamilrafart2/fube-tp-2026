package com.tiendaonline.clientes.mappers;

import com.tiendaonline.clientes.dtos.ClienteRequestDTO;
import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import com.tiendaonline.clientes.models.Cliente;

/**
 * Mapper encargado de convertir objetos Cliente y sus respectivos DTOs
 */
public class ClienteMapper {

    private ClienteMapper() {}

    // Convierte un ClienteRequestDTO en una nueva entidad Cliente
    public static Cliente toEntity(ClienteRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return Cliente.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .build();
    }

    // Convierte una entidad Cliente en un ClienteResponseDTO
    public static ClienteResponseDTO toDTO(Cliente entidad) {
        if (entidad == null) {
            return null;
        }
        return ClienteResponseDTO.builder()
                .id(entidad.getId())
                .nombre(entidad.getNombre())
                .email(entidad.getEmail())
                .telefono(entidad.getTelefono())
                .direccion(entidad.getDireccion())
                .creadoEn(entidad.getCreadoEn())
                .build();
    }

}