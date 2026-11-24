package com.tiendaonline.clientes.exceptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas para verificar que ManejadorGlobalExcepciones intercepta las excepciones
 */
@WebMvcTest(controllers = ManejadorGlobalExcepcionesTest.ControladorDePrueba.class)
@Import({ManejadorGlobalExcepciones.class, ManejadorGlobalExcepcionesTest.ControladorDePrueba.class})
public class ManejadorGlobalExcepcionesTest {

    @Autowired
    private MockMvc mockMvc;

    record DtoDePrueba(
            @NotBlank(message = "no puede estar vacío") String nombre,
            @Email(message = "debe ser un email válido") String email
    ) {}

    @RestController
    static class ControladorDePrueba {
        @GetMapping("/test/no-encontrado")
        void lanzarNoEncontrado() {
            throw new RecursoNoEncontradoException("Cliente con id 99 no encontrado");
        }

        @GetMapping("/test/conflicto")
        void lanzarConflicto() {
            throw new ConflictoException("EMAIL_DUPLICADO", "El email ya está registrado");
        }

        @GetMapping("/test/no-disponible")
        void lanzarNoDisponible() {
            throw new ServicioNoDisponibleException("El servicio de pedidos no responde");
        }

        @PostMapping("/test/validacion")
        void validarBody(@Valid @RequestBody DtoDePrueba dto) {
        }
    }

    @Test
    void deberiaRetornar404CuandoRecursoNoExiste() throws Exception {
        mockMvc.perform(get("/test/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje").value("Cliente con id 99 no encontrado"))
                .andExpect(jsonPath("$.path").value("/test/no-encontrado"));
    }

    @Test
    void deberiaRetornar409CuandoHayConflicto() throws Exception {
        mockMvc.perform(get("/test/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("EMAIL_DUPLICADO"))
                .andExpect(jsonPath("$.mensaje").value("El email ya está registrado"))
                .andExpect(jsonPath("$.path").value("/test/conflicto"));
    }

    @Test
    void deberiaRetornar503CuandoServicioExternoFalla() throws Exception {
        mockMvc.perform(get("/test/no-disponible"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("SERVICIO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.mensaje").value("El servicio de pedidos no responde"));
    }

    @Test
    void deberiaRetornar400ConTodosLosDetallesCuandoFallaValidacion() throws Exception {
        mockMvc.perform(post("/test/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\", \"email\": \"email-invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

}
