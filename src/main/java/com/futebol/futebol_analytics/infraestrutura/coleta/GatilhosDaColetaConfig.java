package com.futebol.futebol_analytics.infraestrutura.coleta;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Quando a coleta automática dispara: na subida da aplicação e/ou por agenda. */
@Configuration
class GatilhosDaColetaConfig {

    @Bean
    @ConditionalOnProperty(name = "futebol.coleta.ao-iniciar", havingValue = "true")
    ApplicationRunner coletaAoIniciar(ColetaDasCompeticoesConfiguradas coleta) {
        return argumentos -> coleta.executar();
    }

    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "futebol.coleta.agendada", havingValue = "true")
    static class ColetaAgendada {

        private final ColetaDasCompeticoesConfiguradas coleta;

        ColetaAgendada(ColetaDasCompeticoesConfiguradas coleta) {
            this.coleta = coleta;
        }

        @Scheduled(cron = "${futebol.coleta.cron}")
        void coletar() {
            coleta.executar();
        }
    }
}
