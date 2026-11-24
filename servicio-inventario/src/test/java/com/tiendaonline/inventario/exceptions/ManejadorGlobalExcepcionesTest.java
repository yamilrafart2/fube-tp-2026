package com.tiendaonline.inventario.exceptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
            @NotBlank(message = "no puede estar vacío") String sku,
            @Positive(message = "debe ser mayor a cero") int cantidad
    ) {}

    @RestController
    static class ControladorDePrueba {
        @GetMapping("/test/no-encontrado")
        void lanzarNoEncontrado() {
            throw new RecursoNoEncontradoException("Producto con id 99 no encontrado");
        }

        @GetMapping("/test/conflicto")
        void lanzarConflicto() {
            throw new ConflictoException("STOCK_INSUFICIENTE", "No hay stock disponible para el producto");
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
                .andExpect(jsonPath("$.mensaje").value("Producto con id 99 no encontrado"))
                .andExpect(jsonPath("$.path").value("/test/no-encontrado"));
    }

    @Test
    void deberiaRetornar409CuandoHayConflicto() throws Exception {
        mockMvc.perform(get("/test/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("STOCK_INSUFICIENTE"))
                .andExpect(jsonPath("$.mensaje").value("No hay stock disponible para el producto"))
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
                        .content("{\"sku\": \"\", \"cantidad\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

}
