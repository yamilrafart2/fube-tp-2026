package com.tiendaonline.clientes.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaonline.clientes.clients.dtos.PedidoClienteResponseDTO;
import com.tiendaonline.clientes.dtos.ClienteRequestDTO;
import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import com.tiendaonline.clientes.exceptions.ConflictoException;
import com.tiendaonline.clientes.exceptions.ManejadorGlobalExcepciones;
import com.tiendaonline.clientes.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.clientes.services.ClienteServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del ClienteControlador
 */
@WebMvcTest(ClienteControlador.class)
@Import(ManejadorGlobalExcepciones.class)
public class ClienteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteServicio clienteServicio;

    @Test
    void deberiaRetornar201AlCrearClienteValido() throws Exception {
        ClienteRequestDTO request = ClienteRequestDTO.builder()
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .build();

        ClienteResponseDTO response = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .telefono("+543421234567")
                .direccion("San Martín 1234")
                .creadoEn(OffsetDateTime.now())
                .build();

        when(clienteServicio.crearCliente(any(ClienteRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    void deberiaRetornar400CuandoCrearClienteRecibeDatosInvalidos() throws Exception {
        ClienteRequestDTO requestInvalido = ClienteRequestDTO.builder()
                .nombre("")
                .email("formato-invalido")
                .build();

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }

    @Test
    void deberiaRetornar409CuandoEmailYaExiste() throws Exception {
        ClienteRequestDTO request = ClienteRequestDTO.builder()
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .build();

        when(clienteServicio.crearCliente(any(ClienteRequestDTO.class)))
                .thenThrow(new ConflictoException("EMAIL_DUPLICADO", "Ya existe un cliente con ese email"));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_DUPLICADO"));
    }

    @Test
    void deberiaRetornar200AlObtenerClientePorId() throws Exception {
        ClienteResponseDTO response = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("J*** P***")
                .email("***@e***.com")
                .build();

        when(clienteServicio.obtenerClientePorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("J*** P***"));
    }

    @Test
    void deberiaRetornar404CuandoClienteNoExiste() throws Exception {
        when(clienteServicio.obtenerClientePorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Cliente con id 99 no encontrado"));

        mockMvc.perform(get("/api/v1/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void deberiaRetornar200AlListarClientesPaginados() throws Exception {
        ClienteResponseDTO cliente = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("J*** P***")
                .build();

        Page<ClienteResponseDTO> pagina = new PageImpl<>(List.of(cliente));

        when(clienteServicio.listarClientes(any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/clientes?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void deberiaRetornar200AlObtenerPedidosDeCliente() throws Exception {
        // 1. Arrange
        PedidoClienteResponseDTO pedidoDto = PedidoClienteResponseDTO.builder()
                .id(501L)
                .clienteId(1L)
                .estado("CREADO")
                .build();

        when(clienteServicio.obtenerPedidosDeCliente(1L)).thenReturn(List.of(pedidoDto));

        // 2. Act & 3. Assert
        mockMvc.perform(get("/api/v1/clientes/1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(501));
    }
}