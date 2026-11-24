package com.tiendaonline.pedidos.controllers;

import com.tiendaonline.pedidos.dtos.PedidoRequestDTO;
import com.tiendaonline.pedidos.dtos.PedidoResponseDTO;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.services.PedidoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints HTTP de servicio-pedidos
 */
@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Operaciones para la creación, consulta, confirmación y cancelación de pedidos")
public class PedidoControlador {

    private final PedidoServicio pedidoServicio;

    // Endpoint POST /api/v1/pedidos para crear un nuevo pedido de compra
    @PostMapping
    @Operation(
            summary = "Crear un nuevo pedido",
            description = "Verifica la existencia del cliente, reserva el stock de cada ítem en inventario y calcula el monto total")
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO creado = pedidoServicio.crearPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Endpoint GET /api/v1/pedidos/{id} para consultar un pedido
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener pedido por ID",
            description = "Devuelve los datos completos de un pedido junto con sus ítems y precios históricos")
    public ResponseEntity<PedidoResponseDTO> obtenerPedidoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServicio.obtenerPedidoPorId(id));
    }

    /**
     * Endpoint GET /api/v1/pedidos?clienteId={id}&estado={estado} para listar los pedidos
     * de un cliente, con filtro por estado
     */
    @GetMapping
    @Operation(
            summary = "Listar pedidos de un cliente",
            description = "Devuelve los pedidos de un cliente ordenados del más reciente al más antiguo, con filtro opcional por estado")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidosPorCliente(
            @RequestParam Long clienteId,
            @RequestParam(required = false) EstadoPedido estado) {
        return ResponseEntity.ok(pedidoServicio.listarPedidosPorCliente(clienteId, estado));
    }

    /**
     * Endpoint POST /api/v1/pedidos/{id}/confirmacion para confirmar un pedido que se
     * encuentre en estado CREADO
     */
    @PostMapping("/{id}/confirmacion")
    @Operation(
            summary = "Confirmar un pedido en estado CREADO",
            description = "Transiciona el pedido de CREADO a CONFIRMADO para excluirlo de la expiración nocturna")
    public ResponseEntity<PedidoResponseDTO> confirmarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServicio.confirmarPedido(id));
    }

    /**
     * Endpoint POST /api/v1/pedidos/{id}/cancelacion para cancelar un pedido en estado
     * CREADO o CONFIRMADO
     */
    @PostMapping("/{id}/cancelacion")
    @Operation(
            summary = "Cancelar un pedido y liberar sus reservas",
            description = "Libera masivamente las reservas activas en inventario y pasa el pedido a estado CANCELADO")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServicio.cancelarPedido(id));
    }

}
