package com.tiendaonline.pedidos.controllers;

import com.tiendaonline.pedidos.batch.ExpiracionPedidosScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para el disparo manual del proceso por lotes de expiración de pedidos
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jobs.expiracion-pedidos.endpoint-habilitado", havingValue = "true", matchIfMissing = true)
@Tag(name = "Jobs Batch (Local)", description = "Endpoints de soporte en entorno local para disparar manualmente procesos por lotes")
public class JobControlador {

    private final ExpiracionPedidosScheduler expiracionPedidosScheduler;

    // Enpoint POST /api/v1/jobs/expiracion-pedidos para disparar Job
    @PostMapping("/expiracion-pedidos")
    @Operation(
            summary = "Ejecutar manualmente el job de expiración de pedidos (Solo Local)",
            description = "Dispara de forma inmediata el proceso batch que busca pedidos en estado CREADO con más de 24 horas de antigüedad, libera sus reservas en servicio-inventario y los marca como EXPIRADO."
    )
    public ResponseEntity<Map<String, Object>> ejecutarExpiracionPedidos() throws Exception {
        JobExecution execution = expiracionPedidosScheduler.ejecutarJob();
        return ResponseEntity.ok(Map.of(
                "jobId", execution.getJobId(),
                "estado", execution.getStatus().name()
        ));
    }

}
