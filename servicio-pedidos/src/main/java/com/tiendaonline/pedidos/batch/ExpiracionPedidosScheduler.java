package com.tiendaonline.pedidos.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler del proceso por lotes de expiración de pedidos
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ExpiracionPedidosScheduler {

    private final JobLauncher jobLauncher;

    private final Job expiracionPedidosJob;

    public JobExecution ejecutarJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        return jobLauncher.run(expiracionPedidosJob, params);
    }

    @Scheduled(cron = "${jobs.expiracion-pedidos.cron}")
    public void ejecutarExpiracionProgramada() {
        try {
            log.info("Disparando ejecución programada (cron) del job de expiración de pedidos...");
            ejecutarJob();
        } catch (Exception ex) {
            log.error("Error al ejecutar el job programado de expiración de pedidos: {}", ex.getMessage(), ex);
        }
    }

}
