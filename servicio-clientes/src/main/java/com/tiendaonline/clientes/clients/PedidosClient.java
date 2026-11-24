package com.tiendaonline.clientes.clients;

import com.tiendaonline.clientes.clients.dtos.PedidoClienteResponseDTO;
import com.tiendaonline.clientes.exceptions.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Cliente HTTP encargado de la comunicación desde servicio-clientes
 * hacia servicio-pedidos para consultar los pedidos asociados a un cliente
 */
@Component
public class PedidosClient {

    private final RestClient pedidosRestClient;

    public PedidosClient(@Qualifier("pedidosRestClient") RestClient pedidosRestClient) {
        this.pedidosRestClient = pedidosRestClient;
    }

    public List<PedidoClienteResponseDTO> obtenerPedidosPorCliente(Long clienteId) {
        try {
            return pedidosRestClient.get()
                    .uri("/api/v1/pedidos?clienteId={clienteId}", clienteId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PedidoClienteResponseDTO>>() {});
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("El servicio de pedidos no está disponible en este momento");
        }
    }
}
