package com.tiendaonline.pedidos.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

/**
 * Listener del Job de expiración de pedidos JobExecutionListener
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiracionPedidosJobListener implements JobExecutionListener {

    private final ExpiracionPedidoProcessor processor;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        processor.reiniciarContadorFallidos();
        log.info("Iniciando job de expiración de pedidos (jobExecutionId={})...", jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long leidos = 0;
        long escritos = 0;
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            leidos += stepExecution.getReadCount();
            escritos += stepExecution.getWriteCount();
        }
        int fallidos = processor.getPedidosFallidos();

        log.info("Finalizó job de expiración de pedidos (estado={}): leídos={}, expirados={}, fallidos={}",
                jobExecution.getStatus(), leidos, escritos, fallidos);
    }

}
