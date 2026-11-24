package com.tiendaonline.pedidos.clients;

import com.tiendaonline.pedidos.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.exceptions.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente HTTP encargado de la comunicación de servicio-pedidos
 * hacia servicio-clientes
 */
@Component
public class ClientesClient {

    private final RestClient clientesRestClient;

    public ClientesClient(@Qualifier("clientesRestClient") RestClient clientesRestClient) {
        this.clientesRestClient = clientesRestClient;
    }

    public void verificarClienteExiste(Long clienteId) {
        try {
            clientesRestClient.get()
                    .uri("/api/v1/clientes/{id}", clienteId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("No se encontró el cliente con ID: " + clienteId);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("El servicio de clientes no está disponible en este momento");
        }
    }
}
