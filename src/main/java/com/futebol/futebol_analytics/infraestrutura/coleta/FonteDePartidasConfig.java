package com.futebol.futebol_analytics.infraestrutura.coleta;

import com.futebol.futebol_analytics.aplicacao.porta.FonteDePartidas;
import com.futebol.futebol_analytics.infraestrutura.config.ColetaProperties;
import com.futebol.futebol_analytics.infraestrutura.config.FootballDataProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestClient;

/**
 * Escolhe a implementação de {@link FonteDePartidas} por configuração
 * ({@code futebol.coleta.fonte}). Uma fonte nova entra como mais um bean aqui,
 * sem tocar nos casos de uso.
 */
@Configuration
class FonteDePartidasConfig {

    @Bean
    @ConditionalOnProperty(name = "futebol.coleta.fonte", havingValue = "arquivo", matchIfMissing = true)
    FonteDePartidas fonteArquivoCsv(ColetaProperties coleta, ResourceLoader resourceLoader) {
        return new FonteArquivoCsv(resourceLoader.getResource(coleta.arquivo()));
    }

    @Bean
    @ConditionalOnProperty(name = "futebol.coleta.fonte", havingValue = "football-data")
    FonteDePartidas fonteFootballData(FootballDataProperties propriedades) {
        if (propriedades.token() == null || propriedades.token().isBlank()) {
            throw new IllegalStateException(
                    "futebol.coleta.fonte=football-data exige o token em FOOTBALL_DATA_TOKEN");
        }
        RestClient http = RestClient.builder()
                .baseUrl(propriedades.urlBase())
                .defaultHeader("X-Auth-Token", propriedades.token())
                .build();
        return new FonteFootballData(http);
    }
}
