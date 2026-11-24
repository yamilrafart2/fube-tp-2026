package com.tiendaonline.clientes.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de la documentación Swagger UI para servicio-clientes
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Servicio de Clientes",
                version = "1.0.0",
                description = "Microservicio encargado de la gestión de clientes y consulta de su historial de pedidos."
        )
)
public class OpenApiConfig {
}