package com.futebol.futebol_analytics.infraestrutura.config;

import com.futebol.futebol_analytics.dominio.Competicao;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração da coleta ({@code futebol.coleta.*}).
 *
 * @param fonte       {@code arquivo} (CSV local, padrão) ou {@code football-data} (API)
 * @param arquivo     localização do CSV usado pela fonte {@code arquivo}
 * @param aoIniciar   coleta as competições configuradas assim que a aplicação sobe
 * @param agendada    liga a coleta periódica
 * @param cron        expressão cron da coleta periódica
 * @param competicoes competições coletadas automaticamente
 */
@ConfigurationProperties(prefix = "futebol.coleta")
public record ColetaProperties(
        String fonte,
        String arquivo,
        boolean aoIniciar,
        boolean agendada,
        String cron,
        List<CompeticaoConfigurada> competicoes) {

    public ColetaProperties {
        competicoes = competicoes == null ? List.of() : List.copyOf(competicoes);
    }

    public record CompeticaoConfigurada(String codigo, int temporada) {

        public Competicao paraDominio() {
            return new Competicao(codigo, temporada);
        }
    }
}
