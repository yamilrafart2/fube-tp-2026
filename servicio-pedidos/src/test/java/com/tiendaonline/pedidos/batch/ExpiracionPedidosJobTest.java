package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clients.InventarioClient;
import com.tiendaonline.pedidos.exceptions.ServicioNoDisponibleException;
import com.tiendaonline.pedidos.models.EstadoPedido;
import com.tiendaonline.pedidos.models.Pedido;
import com.tiendaonline.pedidos.repositories.PedidoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Pruebas dpara el proceso por lotes de expiración de pedidos
 */
@SpringBatchTest
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pedidos_batch_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.batch.job.enabled=false",
        "spring.batch.jdbc.initialize-schema=always"
})
class ExpiracionPedidosJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @MockBean
    private InventarioClient inventarioClient;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        pedidoRepositorio.deleteAll();
    }

    private Long crearPedidoConFecha(EstadoPedido estado, OffsetDateTime fechaCreacion) {
        Pedido pedido = Pedido.builder()
                .clienteId(1L)
                .estado(estado)
                .montoTotal(new BigDecimal("45000.00"))
                .fechaCreacion(fechaCreacion)
                .fechaActualizacion(fechaCreacion)
                .build();
        return pedidoRepositorio.save(pedido).getId();
    }

    @Test
    @DisplayName("Debe expirar solo pedidos en estado CREADO con más de 24h y no tocar recientes ni confirmados")
    void deberiaExpirarSoloPedidosCreadosConMasDe24HorasYNoTocarRecientesNiConfirmados() throws Exception {
        Long idVencido = crearPedidoConFecha(EstadoPedido.CREADO, OffsetDateTime.now().minusHours(25));
        Long idReciente = crearPedidoConFecha(EstadoPedido.CREADO, OffsetDateTime.now().minusHours(2));
        Long idConfirmadoVencido = crearPedidoConFecha(EstadoPedido.CONFIRMADO, OffsetDateTime.now().minusHours(30));

        JobExecution execution = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());

        assertAll(
                () -> assertEquals(BatchStatus.COMPLETED, execution.getStatus()),
                () -> assertEquals(EstadoPedido.EXPIRADO, pedidoRepositorio.findById(idVencido).orElseThrow().getEstado()),
                () -> assertEquals(EstadoPedido.CREADO, pedidoRepositorio.findById(idReciente).orElseThrow().getEstado()),
                () -> assertEquals(EstadoPedido.CONFIRMADO, pedidoRepositorio.findById(idConfirmadoVencido).orElseThrow().getEstado())
        );
        verify(inventarioClient, times(1)).liberarReservasPorPedido(idVencido);
    }

    @Test
    @DisplayName("Debe ser idempotente: una segunda corrida seguida no debe volver a expirar pedidos")
    void deberiaSerIdempotenteEnSegundaCorridaSeguida() throws Exception {
        Long idVencido = crearPedidoConFecha(EstadoPedido.CREADO, OffsetDateTime.now().minusHours(26));

        JobExecution primeraCorrida = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());
        StepExecution stepPrimera = primeraCorrida.getStepExecutions().iterator().next();

        JobExecution segundaCorrida = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());
        StepExecution stepSegunda = segundaCorrida.getStepExecutions().iterator().next();

        assertAll(
                () -> assertEquals(BatchStatus.COMPLETED, primeraCorrida.getStatus()),
                () -> assertEquals(1, stepPrimera.getWriteCount()),
                () -> assertEquals(BatchStatus.COMPLETED, segundaCorrida.getStatus()),
                () -> assertEquals(0, stepSegunda.getReadCount()),
                () -> assertEquals(0, stepSegunda.getWriteCount())
        );
        verify(inventarioClient, times(1)).liberarReservasPorPedido(idVencido);
    }

    @Test
    @DisplayName("Debe continuar procesando el resto del lote si falla la liberación de un pedido en inventario")
    void deberiaContinuarConElRestoDelLoteSiFallaLiberacionDeUnPedido() throws Exception {
        Long idPedidoQueFalla = crearPedidoConFecha(EstadoPedido.CREADO, OffsetDateTime.now().minusHours(25));
        Long idPedidoExitoso = crearPedidoConFecha(EstadoPedido.CREADO, OffsetDateTime.now().minusHours(25));

        doThrow(new ServicioNoDisponibleException("Servicio de inventario caído"))
                .when(inventarioClient).liberarReservasPorPedido(idPedidoQueFalla);

        JobExecution execution = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());
        StepExecution stepExecution = execution.getStepExecutions().iterator().next();

        assertAll(
                () -> assertEquals(BatchStatus.COMPLETED, execution.getStatus()),
                () -> assertEquals(2, stepExecution.getReadCount()),
                () -> assertEquals(1, stepExecution.getWriteCount()),
                () -> assertEquals(EstadoPedido.CREADO, pedidoRepositorio.findById(idPedidoQueFalla).orElseThrow().getEstado()),
                () -> assertEquals(EstadoPedido.EXPIRADO, pedidoRepositorio.findById(idPedidoExitoso).orElseThrow().getEstado())
        );
    }

    @Test
    @DisplayName("Debe procesar múltiples chunks sin saltear registros cuando el conjunto de pedidos CREADO se achica")
    void deberiaProcesarMultiplesChunksSinSaltearRegistros() throws Exception {
        List<Long> idsCreados = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            idsCreados.add(crearPedidoConFecha(EstadoPedido.CREADO, OffsetDateTime.now().minusHours(25)));
        }

        JobExecution execution = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());
        StepExecution stepExecution = execution.getStepExecutions().iterator().next();

        assertAll(
                () -> assertEquals(BatchStatus.COMPLETED, execution.getStatus()),
                () -> assertEquals(15, stepExecution.getReadCount()),
                () -> assertEquals(15, stepExecution.getWriteCount())
        );

        for (Long id : idsCreados) {
            assertEquals(EstadoPedido.EXPIRADO, pedidoRepositorio.findById(id).orElseThrow().getEstado());
            verify(inventarioClient, times(1)).liberarReservasPorPedido(id);
        }
    }
}