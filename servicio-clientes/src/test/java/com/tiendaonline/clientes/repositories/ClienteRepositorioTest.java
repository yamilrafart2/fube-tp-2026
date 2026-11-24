package com.tiendaonline.clientes.repositories;

import com.tiendaonline.clientes.models.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del ClienteRepositorio
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ClienteRepositorioTest {

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Test
    void deberiaGuardarYBuscarPorId() {
        // 1. Arrange
        Cliente nuevo = Cliente.builder()
                .nombre("Ana Gómez")
                .email("ana@email.com")
                .telefono("+543424111111")
                .direccion("Bv. Gálvez 1500")
                .build();

        // 2. Act
        Cliente guardado = clienteRepositorio.save(nuevo);

        // 3. Assert
        assertNotNull(guardado.getId());
        assertNotNull(guardado.getCreadoEn());

        Optional<Cliente> encontrado = clienteRepositorio.findById(guardado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("ana@email.com", encontrado.get().getEmail());
    }

    @Test
    void deberiaRetornarTrueSiEmailExisteYFalseSiNoExiste() {
        // 1. Arrange
        Cliente nuevo = Cliente.builder()
                .nombre("Carlos Ruiz")
                .email("carlos@email.com")
                .build();

        clienteRepositorio.save(nuevo);

        // 2. Act & 3. Assert
        assertTrue(clienteRepositorio.existsByEmail("carlos@email.com"));
        assertFalse(clienteRepositorio.existsByEmail("inexistente@email.com"));
    }

    @Test
    void deberiaBuscarClientePorEmail() {
        // 1. Arrange
        Cliente nuevo = Cliente.builder()
                .nombre("Lucia Diaz")
                .email("lucia@email.com")
                .build();

        clienteRepositorio.save(nuevo);

        // 2. Act
        Optional<Cliente> resultado = clienteRepositorio.findByEmail("lucia@email.com");

        // 3. Assert
        assertTrue(resultado.isPresent());
        assertEquals("Lucia Diaz", resultado.get().getNombre());
    }

}
