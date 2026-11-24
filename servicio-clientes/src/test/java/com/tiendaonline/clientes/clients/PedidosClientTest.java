package com.tiendaonline.clientes.clients;

import com.tiendaonline.clientes.clients.dtos.PedidoClienteResponseDTO;
import com.tiendaonline.clientes.config.RestClientConfig;
import com.tiendaonline.clientes.exceptions.ServicioNoDisponibleException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Pruebas del cliente HTTP PedidosClient
 */
public class PedidosClientTest {

    private static ClientAndServer mockServer;

    private PedidosClient pedidosClient;

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
        RestClient restClient = config.pedidosRestClient(RestClient.builder(), baseUrl, 1000, 1000);
        pedidosClient = new PedidosClient(restClient);
    }

    @Test
    void deberiaObtenerListaDePedidosPorClienteCuandoResponde200() {
        // 1. Arrange
        String jsonRespuesta = """
                [
                  {
                    "id": 501,
                    "clienteId": 1,
                    "estado": "CREADO",
                    "montoTotal": 90000.00,
                    "listaItems": [
                      {
                        "id": 1,
                        "productoId": 10,
                        "cantidad": 2,
                        "precioUnitario": 45000.00
                      }
                    ]
                  }
                ]
                """;

        mockServer.when(
                request().withMethod("GET")
                        .withPath("/api/v1/pedidos")
                        .withQueryStringParameter("clienteId", "1")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(jsonRespuesta)
        );

        // 2. Act
        List<PedidoClienteResponseDTO> pedidos = pedidosClient.obtenerPedidosPorCliente(1L);

        // 3. Assert
        assertEquals(1, pedidos.size());
        assertEquals(501L, pedidos.getFirst().getId());
        assertEquals(1, pedidos.getFirst().getListaItems().size());
    }

    @Test
    void deberiaLanzarServicioNoDisponibleExceptionCuandoPedidosResponde500() {
        // 1. Arrange
        mockServer.when(
                request().withMethod("GET")
                        .withPath("/api/v1/pedidos")
                        .withQueryStringParameter("clienteId", "1")
        ).respond(
                response().withStatusCode(500)
        );

        // 2. Act & 3. Assert
        assertThrows(ServicioNoDisponibleException.class, () ->
                pedidosClient.obtenerPedidosPorCliente(1L));
    }

}
