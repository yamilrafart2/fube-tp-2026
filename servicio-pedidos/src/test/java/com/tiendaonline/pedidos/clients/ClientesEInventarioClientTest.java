package com.tiendaonline.pedidos.clients;

import com.tiendaonline.pedidos.clients.dtos.ReservaInventarioResponseDTO;
import com.tiendaonline.pedidos.config.RestClientConfig;
import com.tiendaonline.pedidos.exceptions.ConflictoException;
import com.tiendaonline.pedidos.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.exceptions.ServicioNoDisponibleException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Pruebas de clientes HTTP ClientesClient e InventarioClient
 */
public class ClientesEInventarioClientTest {

    private static ClientAndServer mockServer;

    private ClientesClient clientesClient;

    private InventarioClient inventarioClient;

    @BeforeAll
    static void iniciarMockServer() {
        mockServer = ClientAndServer.startClientAndServer(0);
    }

    @AfterAll
    static void detenerMockServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @BeforeEach
    void configurarClientes() {
        mockServer.reset();
        String baseUrl = "http://localhost:" + mockServer.getLocalPort();
        RestClientConfig config = new RestClientConfig();
        RestClient restClient = config.clientesRestClient(RestClient.builder(), baseUrl, 1000, 1000);
        clientesClient = new ClientesClient(restClient);
        inventarioClient = new InventarioClient(restClient);
    }

    @Test
    void deberiaVerificarClienteExistenteCuandoResponde200() {
        // 1. Arrange
        mockServer.when(
                request().withMethod("GET").withPath("/api/v1/clientes/1")
        ).respond(
                response().withStatusCode(200)
        );

        // 2. Act & 3. Assert
        assertDoesNotThrow(() -> clientesClient.verificarClienteExiste(1L));
    }

    @Test
    void deberiaLanzarRecursoNoEncontradoExceptionCuandoClienteResponde404() {
        // 1. Arrange
        mockServer.when(
                request().withMethod("GET").withPath("/api/v1/clientes/99")
        ).respond(
                response().withStatusCode(404)
        );

        // 2. Act & 3. Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> clientesClient.verificarClienteExiste(99L));
    }

    @Test
    void deberiaLanzarServicioNoDisponibleExceptionCuandoClientesResponde500() {
        // 1. Arrange
        mockServer.when(
                request().withMethod("GET").withPath("/api/v1/clientes/1")
        ).respond(
                response().withStatusCode(500)
        );

        // 2. Act & 3. Assert
        assertThrows(ServicioNoDisponibleException.class,
                () -> clientesClient.verificarClienteExiste(1L));
    }

    @Test
    void deberiaReservarStockYDeserializarPrecioUnitarioCuandoResponde201() {
        // 1. Arrange
        String jsonRespuesta = """
                {
                  "id": 77,
                  "productoId": 10,
                  "pedidoId": 501,
                  "cantidad": 2,
                  "estado": "ACTIVA",
                  "precioUnitario": 45000.00
                }
                """;

        mockServer.when(
                request().withMethod("POST").withPath("/api/v1/reservas")
        ).respond(
                response()
                        .withStatusCode(201)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(jsonRespuesta)
        );

        // 2. Act
        ReservaInventarioResponseDTO respuesta = inventarioClient.reservarStock(10L, 501L, 2);

        // 3. Assert
        assertNotNull(respuesta);
        assertEquals(77L, respuesta.getId());
        assertEquals("ACTIVA", respuesta.getEstado());
        assertEquals(new BigDecimal("45000.00"), respuesta.getPrecioUnitario());
    }

    @Test
    void deberiaLanzarConflictoExceptionCuandoInventarioResponde409() {
        // 1. Arrange
        mockServer.when(
                request().withMethod("POST").withPath("/api/v1/reservas")
        ).respond(
                response().withStatusCode(409)
        );

        // 2. Act & 3. Assert
        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> inventarioClient.reservarStock(10L, 501L, 50));

        assertEquals("STOCK_INSUFICIENTE", ex.getCodigoError());
    }

    @Test
    void deberiaLiberarReservasPorPedidoCuandoResponde200() {
        // 1. Arrange
        mockServer.when(
                request().withMethod("POST").withPath("/api/v1/reservas/liberacion")
        ).respond(
                response().withStatusCode(200)
        );

        // 2. Act & 3. Assert
        assertDoesNotThrow(() -> inventarioClient.liberarReservasPorPedido(501L));
    }
}