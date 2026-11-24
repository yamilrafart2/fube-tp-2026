package com.tiendaonline.clientes.utils;

import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para verificar las reglas de enmascaramiento de EnmascaradorPii
 */
public class EnmascaradorPiiTest {

    @Test
    void deberiaEnmascararNombre() {
        String valorObtenido = EnmascaradorPii.enmascararNombre("Juan Pérez");

        assertEquals("J*** P***", valorObtenido);
        assertNull(EnmascaradorPii.enmascararNombre(null));
    }

    @Test
    void deberiaEnmascararEmailCorrectamente() {
        String valorObtenido = EnmascaradorPii.enmascararEmail("juan@email.com");

        assertEquals("***@e***.com", valorObtenido);
        assertNull(EnmascaradorPii.enmascararEmail(null));
    }

    @Test
    void deberiaEnmascararTelefonoMostrandoUltimosCuatroDigitos() {
        String valorObtenido = EnmascaradorPii.enmascararTelefono("+543421234567");

        assertEquals("***4567", valorObtenido);
        assertEquals("***", EnmascaradorPii.enmascararTelefono("1234"));
        assertNull(EnmascaradorPii.enmascararTelefono(null));
    }

    @Test
    void deberiaEnmascararDireccionCompletamente() {
        String valorObtenido = EnmascaradorPii.enmascararDireccion("San Martín 1234");

        assertEquals("***", valorObtenido);
        assertNull(EnmascaradorPii.enmascararDireccion(null));
    }

    @Test
    void deberiaEnmascararCamposSensiblesDelClienteResponseDto() {
        OffsetDateTime ahora = OffsetDateTime.now();
        ClienteResponseDTO dtoOriginal = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .creadoEn(ahora)
                .build();

        ClienteResponseDTO dtoEnmascarado = EnmascaradorPii.enmascarar(dtoOriginal);

        assertNotNull(dtoEnmascarado);
        assertEquals(1L, dtoEnmascarado.getId());
        assertEquals(ahora, dtoEnmascarado.getCreadoEn());
        assertEquals("J*** P***", dtoEnmascarado.getNombre());
        assertEquals("***@e***.com", dtoEnmascarado.getEmail());
        assertEquals("***4567", dtoEnmascarado.getTelefono());
        assertEquals("***", dtoEnmascarado.getDireccion());
    }

}
