package com.tiendaonline.pedidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaonline.pedidos.dtos.ItemPedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoResponseDTO;
import com.tiendaonline.pedidos.exceptions.ManejadorGlobalExcepciones;
import com.tiendaonline.pedidos.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.services.PedidoServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del PedidoControlador
 */
@WebMvcTest(PedidoControlador.class)
@Import(ManejadorGlobalExcepciones.class)
public class PedidoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoServicio pedidoServicio;

    @Test
    void deberiaRetornar201AlCrearPedidoValido() throws Exception {
        // 1. Arrange
        ItemPedidoRequestDTO itemDTO = ItemPedidoRequestDTO.builder()
                .productoId(10L)
                .cantidad(2)
                .build();

        List<ItemPedidoRequestDTO> listaItemsDTO = new ArrayList<>();
        listaItemsDTO.add(itemDTO);

        PedidoRequestDTO requestDTO = PedidoRequestDTO.builder()
                .clienteId(1L)
                .listaItems(listaItemsDTO)
                .build();

        PedidoResponseDTO responseDTO = PedidoResponseDTO.builder()
                .id(501L)
                .clienteId(1L)
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.valueOf(90000.00))
                .build();

        when(pedidoServicio.crearPedido(any(PedidoRequestDTO.class))).thenReturn(responseDTO);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.estado").value("CREADO"));
    }

    @Test
    void deberiaRetornar400CuandoCrearPedidoRecibeDatosInvalidos() throws Exception {
        // 1. Arrange
        PedidoRequestDTO requestDTO = PedidoRequestDTO.builder()
                .clienteId(null)
                .listaItems(List.of())
                .build();

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

    @Test
    void deberiaRetornar200AlObtenerPedidoPorId() throws Exception {
        // 1. Arrange
        PedidoResponseDTO responseDTO = PedidoResponseDTO.builder()
                .id(501L)
                .clienteId(1L)
                .build();

        when(pedidoServicio.obtenerPedidoPorId(501L)).thenReturn(responseDTO);

        // 2. Act & 3. Assert
        mockMvc.perform(get("/api/v1/pedidos/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(501));
    }

    @Test
    void deberiaRetornar404CuandoPedidoNoExiste() throws Exception {
        // 1. Arrange
        when(pedidoServicio.obtenerPedidoPorId(999L))
                .thenThrow(new RecursoNoEncontradoException("Pedido con id 999 no encontrado"));

        // 2. Act & 3. Assert
        mockMvc.perform(get("/api/v1/pedidos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void deberiaRetornar200AlListarPedidosPorCliente() throws Exception {
        // 1. Arrange
        PedidoResponseDTO responseDTO = PedidoResponseDTO.builder()
                .id(501L)
                .clienteId(1L)
                .build();

        when(pedidoServicio.listarPedidosPorCliente(1L, null)).thenReturn(List.of(responseDTO));

        // 2. Act & 3. Assert
        mockMvc.perform(get("/api/v1/pedidos?clienteId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(501));
    }

    @Test
    void deberiaRetornar200AlConfirmarPedido() throws Exception {
        // 1. Arrange
        PedidoResponseDTO responseConfirmado = PedidoResponseDTO.builder()
                .id(501L)
                .clienteId(1L)
                .estado(EstadoPedido.CONFIRMADO)
                .build();

        when(pedidoServicio.confirmarPedido(501L)).thenReturn(responseConfirmado);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/v1/pedidos/501/confirmacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void deberiaRetornar200AlCancelarPedido() throws Exception {
        // 1. Arrange
        PedidoResponseDTO responseCancelado = PedidoResponseDTO.builder()
                .id(501L)
                .clienteId(1L)
                .estado(EstadoPedido.CANCELADO)
                .build();

        when(pedidoServicio.cancelarPedido(501L)).thenReturn(responseCancelado);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/v1/pedidos/501/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

}
