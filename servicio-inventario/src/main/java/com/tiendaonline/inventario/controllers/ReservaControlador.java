package com.tiendaonline.inventario.controllers;

import com.tiendaonline.inventario.dtos.LiberacionPorPedidoRequestDTO;
import com.tiendaonline.inventario.dtos.LiberacionPorPedidoResponseDTO;
import com.tiendaonline.inventario.dtos.ReservaRequestDTO;
import com.tiendaonline.inventario.dtos.ReservaResponseDTO;
import com.tiendaonline.inventario.services.InventarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints HTTP de servicio-inventario
 */
@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Operaciones para reservar y liberar stock de productos")
public class ReservaControlador {

    private final InventarioServicio inventarioServicio;

    // Endpoint POST /api/v1/reservas para reservar stock de un producto asociado a un pedido
    @PostMapping
    @Operation(summary = "Reservar stock de un producto para un pedido", description = "Descuenta stock atómicamente y crea una reserva ACTIVA devolviendo el precio unitario")
    public ResponseEntity<ReservaResponseDTO> reservarStock(@Valid @RequestBody ReservaRequestDTO request) {
        ReservaResponseDTO creado = inventarioServicio.reservarStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Endpoint POST /api/v1/reservas/{id}/liberacion para liberar de una reserva
    @PostMapping("/{id}/liberacion")
    @Operation(summary = "Liberar una reserva individual de forma idempotente", description = "Pasa la reserva a LIBERADA y restituye el stock si se encontraba ACTIVA")
    public ResponseEntity<ReservaResponseDTO> liberarReserva(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioServicio.liberarReserva(id));
    }

    // Endpoint POST /api/v1/reservas/liberacion para liberar de forma masiva
    // todas las reservas activas asociadas a un pedido
    @PostMapping("/liberacion")
    @Operation(summary = "Liberar masivamente todas las reservas activas de un pedido", description = "Libera todas las reservas ACTIVAS del pedido indicado y devuelve el conteo procesado")
    public ResponseEntity<LiberacionPorPedidoResponseDTO> liberarReservasPorPedido(
            @Valid @RequestBody LiberacionPorPedidoRequestDTO request) {
        return ResponseEntity.ok(inventarioServicio.liberarReservasPorPedido(request));
    }

}
