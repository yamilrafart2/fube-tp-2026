package com.tiendaonline.inventario.services;

import com.tiendaonline.inventario.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventarioServicio {

    ProductoResponseDTO crearProducto(ProductoRequestDTO dto);

    ProductoResponseDTO obtenerProductoPorId(Long id);

    Page<ProductoResponseDTO> listarProductos(Pageable pageable);

    ReservaResponseDTO reservarStock(ReservaRequestDTO dto);

    ReservaResponseDTO liberarReserva(Long reservaId);
    
    LiberacionPorPedidoResponseDTO liberarReservasPorPedido(LiberacionPorPedidoRequestDTO dto);

}