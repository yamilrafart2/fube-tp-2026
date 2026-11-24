package com.tiendaonline.pedidos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de la documentación Swagger UI para servicio-pedidos
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Servicio de Pedidos",
                version = "1.0.0",
                description = "Microservicio encargado del ciclo de vida de pedidos, coordinación de reservas y expiración por lotes."
        )
)
public class OpenApiConfig {
}