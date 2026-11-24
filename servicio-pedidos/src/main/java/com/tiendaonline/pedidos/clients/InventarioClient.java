package com.tiendaonline.pedidos.clients;

import com.tiendaonline.pedidos.clients.dtos.LiberacionPorPedidoRequestDTO;
import com.tiendaonline.pedidos.clients.dtos.ReservaInventarioRequestDTO;
import com.tiendaonline.pedidos.clients.dtos.ReservaInventarioResponseDTO;
import com.tiendaonline.pedidos.exceptions.ConflictoException;
import com.tiendaonline.pedidos.exceptions.RecursoNoEncontradoException;
import com.tiendaonline.pedidos.exceptions.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente HTTP encargado de la comunicación de servicio-pedidos
 * hacia servicio-inventario
 */
@Component
public class InventarioClient {

    private final RestClient inventarioRestClient;

    public InventarioClient(@Qualifier("inventarioRestClient") RestClient inventarioRestClient) {
        this.inventarioRestClient = inventarioRestClient;
    }

    public ReservaInventarioResponseDTO reservarStock(Long productoId, Long pedidoId, Integer cantidad) {
        ReservaInventarioRequestDTO request = ReservaInventarioRequestDTO.builder()
                .productoId(productoId)
                .pedidoId(pedidoId)
                .cantidad(cantidad)
                .build();
        try {
            return inventarioRestClient.post()
                    .uri("/api/v1/reservas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ReservaInventarioResponseDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("No se encontró el producto con ID: " + productoId);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new ConflictoException("STOCK_INSUFICIENTE",
                    "Stock insuficiente para el producto con ID: " + productoId);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException(
                    "El servicio de inventario no está disponible en este momento");
        }
    }

    public void liberarReservasPorPedido(Long pedidoId) {
        LiberacionPorPedidoRequestDTO request = LiberacionPorPedidoRequestDTO.builder()
                .pedidoId(pedidoId)
                .build();
        try {
            inventarioRestClient.post()
                    .uri("/api/v1/reservas/liberacion")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException(
                    "No se pudieron liberar las reservas en el servicio de inventario");
        }
    }

}