package com.tiendaonline.inventario.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de la documentación Swagger UI para servicio-inventario
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Servicio de Inventario",
                version = "1.0.0",
                description = "Microservicio encargado del catálogo de productos, control de stock y gestión de reservas."
        )
)
public class OpenApiConfig {
}