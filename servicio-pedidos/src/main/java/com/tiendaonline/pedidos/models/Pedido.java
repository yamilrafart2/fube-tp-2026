package com.tiendaonline.pedidos.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un pedido y mapea a la tabla pedidos de la base de datos
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemPedido> listaItems = new ArrayList<>();

    public void agregarItem(ItemPedido item) {
        this.listaItems.add(item);
        item.setPedido(this);
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (this.fechaCreacion == null) {
            this.fechaCreacion = ahora;
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = ahora;
        }
        if (this.montoTotal == null) {
            this.montoTotal = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = OffsetDateTime.now();
    }

}
