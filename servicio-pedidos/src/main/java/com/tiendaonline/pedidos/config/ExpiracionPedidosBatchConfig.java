package com.tiendaonline.pedidos.config;

import com.tiendaonline.pedidos.batch.ExpiracionPedidoProcessor;
import com.tiendaonline.pedidos.batch.ExpiracionPedidosJobListener;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.Pedido;
import com.tiendaonline.pedidos.repositories.PedidoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Configuración de Spring Batch para el proceso por lotes de expiración de pedidos
 */
@Configuration
@RequiredArgsConstructor
public class ExpiracionPedidosBatchConfig {

    private static final int TAMANO_CHUNK = 10;

    private final PedidoRepositorio pedidoRepositorio;
    private final ExpiracionPedidoProcessor expiracionPedidoProcessor;
    private final ExpiracionPedidosJobListener expiracionPedidosJobListener;

    @Bean
    @StepScope
    public RepositoryItemReader<Pedido> pedidosExpirablesReader() {
        RepositoryItemReader<Pedido> reader = new RepositoryItemReader<>() {

            private boolean ultimaPaginaCompleta = true;

            @Override
            protected List<Pedido> doPageRead() throws Exception {
                if (!ultimaPaginaCompleta) {
                    return List.of();
                }
                if (expiracionPedidoProcessor.getPedidosFallidos() == 0) {
                    jumpToItem(0);
                }
                List<Pedido> pagina = super.doPageRead();
                ultimaPaginaCompleta = (pagina.size() == TAMANO_CHUNK);
                return pagina;
            }
        };

        reader.setName("pedidosExpirablesReader");
        reader.setRepository(pedidoRepositorio);
        reader.setMethodName("findByEstadoAndFechaCreacionBefore");
        reader.setArguments(List.of(EstadoPedido.CREADO, OffsetDateTime.now().minusHours(24)));
        reader.setPageSize(TAMANO_CHUNK);
        reader.setSort(Map.of("id", Sort.Direction.ASC));

        return reader;
    }

    @Bean
    public RepositoryItemWriter<Pedido> pedidosExpiradosWriter() {
        RepositoryItemWriter<Pedido> writer = new RepositoryItemWriter<>();
        writer.setRepository(pedidoRepositorio);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step expiracionPedidosStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            RepositoryItemReader<Pedido> pedidosExpirablesReader,
            RepositoryItemWriter<Pedido> pedidosExpiradosWriter) {

        return new StepBuilder("expiracionPedidosStep", jobRepository)
                .<Pedido, Pedido>chunk(TAMANO_CHUNK, transactionManager)
                .reader(pedidosExpirablesReader)
                .processor(expiracionPedidoProcessor)
                .writer(pedidosExpiradosWriter)
                .build();
    }

    @Bean
    public Job expiracionPedidosJob(JobRepository jobRepository, Step expiracionPedidosStep) {
        return new JobBuilder("expiracionPedidosJob", jobRepository)
                .listener(expiracionPedidosJobListener)
                .start(expiracionPedidosStep)
                .build();
    }
}
