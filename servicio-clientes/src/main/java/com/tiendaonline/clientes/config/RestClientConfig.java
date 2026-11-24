package com.tiendaonline.clientes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente HTTP RestClient
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient pedidosRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${servicios.pedidos.url}") String baseUrl,
            @Value("${servicios.timeouts.connect-ms}") int connectTimeoutMs,
            @Value("${servicios.timeouts.read-ms}") int readTimeoutMs) {
        return crearRestClient(restClientBuilder, baseUrl, connectTimeoutMs, readTimeoutMs);
    }

    private RestClient crearRestClient(
            RestClient.Builder restClientBuilder, String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return restClientBuilder.clone()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

}
