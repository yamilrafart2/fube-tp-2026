package com.tiendaonline.inventario.repositories;

import com.tiendaonline.inventario.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    boolean existsBySku(String sku);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Producto p SET p.cantidadDisponible = p.cantidadDisponible - :cantidad " +
            "WHERE p.id = :productoId AND p.cantidadDisponible >= :cantidad")
    int descontarStock(@Param("productoId") Long productoId, @Param("cantidad") Integer cantidad);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Producto p SET p.cantidadDisponible = p.cantidadDisponible + :cantidad " +
            "WHERE p.id = :productoId")
    int incrementarStock(@Param("productoId") Long productoId, @Param("cantidad") Integer cantidad);

}