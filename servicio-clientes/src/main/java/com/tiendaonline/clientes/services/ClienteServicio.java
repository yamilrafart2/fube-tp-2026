package com.tiendaonline.clientes.services;

import com.tiendaonline.clientes.clients.dtos.PedidoClienteResponseDTO;
import com.tiendaonline.clientes.dtos.ClienteRequestDTO;
import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteServicio {

    ClienteResponseDTO crearCliente(ClienteRequestDTO request);

    ClienteResponseDTO obtenerClientePorId(Long id);

    Page<ClienteResponseDTO> listarClientes(Pageable pageable);

    List<PedidoClienteResponseDTO> obtenerPedidosDeCliente(Long id);

}