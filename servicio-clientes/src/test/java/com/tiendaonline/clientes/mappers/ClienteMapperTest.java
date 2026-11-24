package com.tiendaonline.clientes.mappers;

import com.tiendaonline.clientes.dtos.ClienteRequestDTO;
import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import com.tiendaonline.clientes.models.Cliente;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para ClienteMapper
 */
public class ClienteMapperTest {

    @Test
    void deberiaConvertirRequestDtoAEntidad() {
        // 1. Arrange
        ClienteRequestDTO dto = ClienteRequestDTO.builder()
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543424000000")
                .direccion("San Martín 1234")
                .build();

        // 2. Act
        Cliente entidad = ClienteMapper.toEntity(dto);

        // 3. Assert
        assertNotNull(entidad);
        assertNull(entidad.getId());
        assertEquals("Juan Pérez", entidad.getNombre());
        assertEquals("juan@email.com", entidad.getEmail());
        assertEquals("+543424000000", entidad.getTelefono());
        assertEquals("San Martín 1234", entidad.getDireccion());
    }

    @Test
    void deberiaRetornarNullAlConvertirDtoNuloAEntidad() {
        assertNull(ClienteMapper.toEntity(null));
    }

    @Test
    void deberiaConvertirEntidadAResponseDto() {
        // 1. Arrange
        OffsetDateTime ahora = OffsetDateTime.now();
        Cliente entidad = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543424000000")
                .direccion("San Martín 1234")
                .creadoEn(ahora)
                .build();

        // 2. Act
        ClienteResponseDTO dto = ClienteMapper.toDTO(entidad);

        // 3. Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombre());
        assertEquals("juan@email.com", dto.getEmail());
        assertEquals("+543424000000", dto.getTelefono());
        assertEquals("San Martín 1234", dto.getDireccion());
        assertEquals(ahora, dto.getCreadoEn());
    }

    @Test
    void deberiaRetornarNullAlConvertirEntidadNulaADto() {
        assertNull(ClienteMapper.toDTO(null));
    }
}