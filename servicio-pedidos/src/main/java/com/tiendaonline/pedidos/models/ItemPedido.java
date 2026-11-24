package com.tiendaonline.pedidos.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa un ítem de un Pedido y mapea la tabla items_pedido de la base de datos
 */
@Entity
@Table(name = "items_pedido")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

}
