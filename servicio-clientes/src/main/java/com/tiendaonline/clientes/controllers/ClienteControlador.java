package com.tiendaonline.clientes.controllers;

import com.tiendaonline.clientes.clients.dtos.PedidoClienteResponseDTO;
import com.tiendaonline.clientes.dtos.ClienteRequestDTO;
import com.tiendaonline.clientes.dtos.ClienteResponseDTO;
import com.tiendaonline.clientes.services.ClienteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints HTTP de servicio-clientes
 */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Operaciones para el registro y consulta de clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;

    // Endpoint POST /api/v1/clientes para registrar un nuevo cliente
    @PostMapping
    @Operation(summary = "Registrar un nuevo cliente", description = "Crea un cliente validando que el email sea único")
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO request) {
        ClienteResponseDTO creado = clienteServicio.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Endpoint GET /api/v1/clientes/{id} para consultar un cliente por su ID
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Devuelve los datos de un cliente (enmascarados según el flag mask-pii)")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteServicio.obtenerClientePorId(id));
    }

    // Endpoint GET /api/v1/clientes para listar clientes de forma paginada
    @GetMapping
    @Operation(summary = "Listar clientes paginados", description = "Devuelve una página de clientes registrados")
    public ResponseEntity<Page<ClienteResponseDTO>> listarClientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clienteServicio.listarClientes(PageRequest.of(page, size)));
    }

    // Endpoint GET /api/v1/clientes/{id}/pedidos para consultar todos los pedidos de un cliente
    @GetMapping("/{id}/pedidos")
    @Operation(
            summary = "Listar pedidos de un cliente",
            description = "Verifica que el cliente exista y consulta su historial de pedidos en servicio-pedidos")
    public ResponseEntity<List<PedidoClienteResponseDTO>> obtenerPedidosDeCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteServicio.obtenerPedidosDeCliente(id));
    }

}