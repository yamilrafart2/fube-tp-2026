package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clients.InventarioClient;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.Pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Procesador del proceso por lotes ItemProcessor
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiracionPedidoProcessor implements ItemProcessor<Pedido, Pedido> {

    private final InventarioClient inventarioClient;

    private final AtomicInteger pedidosFallidos = new AtomicInteger(0);

    public void reiniciarContadorFallidos() {
        pedidosFallidos.set(0);
    }

    public int getPedidosFallidos() {
        return pedidosFallidos.get();
    }

    @Nullable
    @Override
    public Pedido process(@NonNull Pedido pedido) {
        try {

            inventarioClient.liberarReservasPorPedido(pedido.getId());

            pedido.setEstado(EstadoPedido.EXPIRADO);
            log.info("Pedido id={} marcado como EXPIRADO y sus reservas fueron liberadas exitosamente.", pedido.getId());

            return pedido;
        } catch (Exception ex) {
            pedidosFallidos.incrementAndGet();
            log.error("Error al liberar reservas del pedido id={}: {}. Se omite su expiración en esta corrida.",
                    pedido.getId(), ex.getMessage());
            return null;
        }
    }
}
