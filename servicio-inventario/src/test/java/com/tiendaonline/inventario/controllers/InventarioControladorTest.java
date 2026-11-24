package com.tiendaonline.inventario.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaonline.inventario.dtos.*;
import com.tiendaonline.inventario.exceptions.ConflictoException;
import com.tiendaonline.inventario.exceptions.ManejadorGlobalExcepciones;
import com.tiendaonline.inventario.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.inventario.models.EstadoReserva;
import com.tiendaonline.inventario.services.InventarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del ProductoControlador
 */
@WebMvcTest({ProductoControlador.class, ReservaControlador.class})
@Import(ManejadorGlobalExcepciones.class)
public class InventarioControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventarioServicio inventarioServicio;

    @Test
    void deberiaRetornar201AlCrearProductoValido() throws Exception {
        ProductoRequestDTO request = ProductoRequestDTO.builder()
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        ProductoResponseDTO response = ProductoResponseDTO.builder()
                .id(1L)
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        when(inventarioServicio.crearProducto(any(ProductoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("TECL-001"));
    }

    @Test
    void deberiaRetornar400CuandoCrearProductoRecibeDatosInvalidos() throws Exception {
        ProductoRequestDTO requestInvalido = ProductoRequestDTO.builder()
                .sku("")
                .nombre("")
                .precio(BigDecimal.valueOf(-10))
                .cantidadDisponible(-5)
                .build();

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles.length()").value(4));
    }

    @Test
    void deberiaRetornar200AlObtenerProductoPorId() throws Exception {
        ProductoResponseDTO response = ProductoResponseDTO.builder()
                .id(1L)
                .sku("TECL-001")
                .nombre("Teclado Mecánico")
                .precio(BigDecimal.valueOf(45000.00))
                .cantidadDisponible(10)
                .build();

        when(inventarioServicio.obtenerProductoPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cantidadDisponible").value(10));
    }

    @Test
    void deberiaRetornar404CuandoProductoNoExiste() throws Exception {
        when(inventarioServicio.obtenerProductoPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No encontrado"));

        mockMvc.perform(get("/api/v1/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void deberiaRetornar201AlReservarStockExitosamente() throws Exception {
        ReservaRequestDTO request = ReservaRequestDTO.builder()
                .productoId(1L)
                .pedidoId(501L)
                .cantidad(2)
                .build();

        ReservaResponseDTO response = ReservaResponseDTO.builder()
                .id(77L)
                .productoId(1L)
                .pedidoId(501L)
                .cantidad(2)
                .estado(EstadoReserva.ACTIVA)
                .precioUnitario(BigDecimal.valueOf(45000.00))
                .fechaCreacion(OffsetDateTime.now())
                .build();

        when(inventarioServicio.reservarStock(any(ReservaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void deberiaRetornar409CuandoStockEsInsuficienteAlReservar() throws Exception {
        ReservaRequestDTO request = ReservaRequestDTO.builder()
                .productoId(1L)
                .pedidoId(501L)
                .cantidad(2)
                .build();

        when(inventarioServicio.reservarStock(any(ReservaRequestDTO.class)))
                .thenThrow(new ConflictoException("STOCK_INSUFICIENTE", "Stock insuficiente"));

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("STOCK_INSUFICIENTE"));
    }

    @Test
    void deberiaRetornar200AlLiberarReservaIndividual() throws Exception {
        ReservaResponseDTO responseLiberada = ReservaResponseDTO.builder()
                .id(77L)
                .productoId(1L)
                .pedidoId(501L)
                .cantidad(2)
                .estado(EstadoReserva.LIBERADA)
                .precioUnitario(BigDecimal.valueOf(45000.00))
                .fechaCreacion(OffsetDateTime.now())
                .build();

        when(inventarioServicio.liberarReserva(77L)).thenReturn(responseLiberada);

        mockMvc.perform(post("/api/v1/reservas/77/liberacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.estado").value("LIBERADA"));
    }

    @Test
    void deberiaRetornar200AlLiberarReservasMasivamentePorPedido() throws Exception {
        LiberacionPorPedidoRequestDTO request = LiberacionPorPedidoRequestDTO.builder()
                .pedidoId(501L)
                .build();

        when(inventarioServicio.liberarReservasPorPedido(any(LiberacionPorPedidoRequestDTO.class)))
                .thenReturn(new LiberacionPorPedidoResponseDTO(501L, 2));

        mockMvc.perform(post("/api/v1/reservas/liberacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(501))
                .andExpect(jsonPath("$.reservasLiberadas").value(2));
    }
}
