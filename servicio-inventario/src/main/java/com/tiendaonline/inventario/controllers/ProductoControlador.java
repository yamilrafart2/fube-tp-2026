package com.tiendaonline.inventario.controllers;

import com.tiendaonline.inventario.dtos.ProductoRequestDTO;
import com.tiendaonline.inventario.dtos.ProductoResponseDTO;
import com.tiendaonline.inventario.services.InventarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints HTTP de servicio-inventario
 */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Operaciones para el alta y consulta de catálogo y stock de productos")
public class ProductoControlador {

    private final InventarioServicio inventarioServicio;

    // Endpoint POST /api/v1/productos para registrar un nuevo producto
    @PostMapping
    @Operation(summary = "Registrar un nuevo producto", description = "Da de alta un producto validando que su SKU sea único")
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        ProductoResponseDTO creado = inventarioServicio.crearProducto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Endpoint GET /api/v1/productos/{id} para consultar un producto
    @GetMapping("/{id}")
    @Operation(summary = "Consultar producto, precio y stock por ID", description = "Devuelve los datos del producto, su precio unitario y cantidad disponible")
    public ResponseEntity<ProductoResponseDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioServicio.obtenerProductoPorId(id));
    }

    // Endpoint GET /api/v1/productos para listar los productos de forma paginada
    @GetMapping
    @Operation(summary = "Listar productos paginados", description = "Devuelve una página de productos del catálogo")
    public ResponseEntity<Page<ProductoResponseDTO>> listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(inventarioServicio.listarProductos(PageRequest.of(page, size)));
    }

}
