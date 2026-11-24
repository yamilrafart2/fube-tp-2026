package com.tiendaonline.pedidos.repositories;

import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = "listaItems")
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = "listaItems")
    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    @EntityGraph(attributePaths = "listaItems")
    List<Pedido> findByClienteIdAndEstadoOrderByFechaCreacionDesc(Long clienteId, EstadoPedido estado);

    Page<Pedido> findByEstadoAndFechaCreacionBefore(
            EstadoPedido estado, OffsetDateTime fechaLimite, Pageable pageable);

}