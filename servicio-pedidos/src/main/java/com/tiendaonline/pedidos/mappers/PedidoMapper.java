package com.tiendaonline.pedidos.mappers;

import com.tiendaonline.pedidos.dtos.ItemPedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.ItemPedidoResponseDTO;
import com.tiendaonline.pedidos.dtos.PedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoResponseDTO;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.ItemPedido;
import com.tiendaonline.pedidos.models.Pedido;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Mapper encargado de convertir objetos Pedido, ItemPedido y sus respectivos DTOs
 */
public class PedidoMapper {

    private PedidoMapper() {}

    // Convierte una entidad ItemPedido en ItemPedidoResponseDTO
    public static ItemPedidoResponseDTO toItemDTO(ItemPedido item) {
        if (item == null) {
            return null;
        }

        return ItemPedidoResponseDTO.builder()
                .id(item.getId())
                .productoId(item.getProductoId())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .build();
    }

    /**
     * Convierte un ItemPedidoRequestDTO en una entidad ItemPedido
     */
    public static ItemPedido toItemEntity(ItemPedidoRequestDTO dto, BigDecimal precioUnitario) {
        if (dto == null) {
            return null;
        }
        return ItemPedido.builder()
                .productoId(dto.getProductoId())
                .cantidad(dto.getCantidad())
                .precioUnitario(precioUnitario)
                .build();
    }

    /**
     * Convierte una entidad Pedido en PedidoResponseDTO
     */
    public static PedidoResponseDTO toPedidoDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        List<ItemPedidoResponseDTO> itemsDto = pedido.getListaItems() == null
                ? Collections.emptyList()
                : pedido.getListaItems().stream()
                .map(PedidoMapper::toItemDTO)
                .toList();

        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .estado(pedido.getEstado())
                .montoTotal(pedido.getMontoTotal())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaActualizacion(pedido.getFechaActualizacion())
                .listaItems(itemsDto)
                .build();
    }

    /**
     * Convierte un PedidoRequestDTO en un Pedido
     */
    public static Pedido toPedidoEntity(PedidoRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return Pedido.builder()
                .clienteId(dto.getClienteId())
                .estado(EstadoPedido.CREADO)
                .montoTotal(BigDecimal.ZERO)
                .build();
    }

}
