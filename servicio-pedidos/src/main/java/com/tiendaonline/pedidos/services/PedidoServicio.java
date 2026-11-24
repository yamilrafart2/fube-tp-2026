package com.tiendaonline.pedidos.services;

import com.tiendaonline.pedidos.dtos.PedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoResponseDTO;
import com.tiendaonline.pedidos.models.EstadoPedido;

import java.util.List;

public interface PedidoServicio {

    PedidoResponseDTO crearPedido(PedidoRequestDTO dto);

    PedidoResponseDTO obtenerPedidoPorId(Long id);

    List<PedidoResponseDTO> listarPedidosPorCliente(Long clienteId, EstadoPedido estado);

    PedidoResponseDTO confirmarPedido(Long id);

    PedidoResponseDTO cancelarPedido(Long id);

}