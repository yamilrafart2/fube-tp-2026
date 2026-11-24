package com.tiendaonline.clientes.services;

import com.statsig.sdk.Statsig;
import com.statsig.sdk.StatsigUser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementación de FeatureFlagServicio con el SDK de servidor de Statsig para evaluar mask-pii
 */
@Slf4j
@Service
public class StatsigFeatureFlagServicioImpl implements FeatureFlagServicio {

    private static final String FLAG_MASK_PII = "mask-pii";

    @Value("${statsig.server-secret-key:}")
    private String serverSecretKey;

    private boolean inicializado = false;

    @PostConstruct
    public void inicializar() {
        if (serverSecretKey == null || serverSecretKey.isBlank()) {
            log.warn("No se configuró statsig.server-secret-key. Se aplicará fail-closed (mask-pii = true por defecto).");
            return;
        }
        try {
            Statsig.initializeAsync(serverSecretKey).get();
            inicializado = true;
            log.info("SDK de Statsig inicializado correctamente.");
        } catch (Exception ex) {
            log.error("Error al inicializar Statsig. Se aplicará fail-closed (mask-pii = true por defecto).", ex);
        }
    }

    @Override
    public boolean debeEnmascararPii(Long clienteId) {
        if (!inicializado) {
            return true;
        }
        try {
            String userId = clienteId != null ? String.valueOf(clienteId) : "anonymous";
            StatsigUser usuario = new StatsigUser(userId);
            return Statsig.checkGateSync(usuario, FLAG_MASK_PII);
        } catch (Exception ex) {
            log.warn("Fallo al evaluar feature flag '{}' en Statsig. Aplicando fail-closed (true).", FLAG_MASK_PII, ex);
            return true;
        }
    }

    @PreDestroy
    public void cerrar() {
        if (inicializado) {
            Statsig.shutdown();
        }
    }

}