package com.tiendaonline.inventario.repositories;

import com.tiendaonline.inventario.models.EstadoReserva;
import com.tiendaonline.inventario.models.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    List<Reserva> findByPedidoIdAndEstado(Long pedidoId, EstadoReserva estado);

}