package com.futebol.futebol_analytics.infraestrutura.config;

import com.futebol.futebol_analytics.aplicacao.ColetarPartidas;
import com.futebol.futebol_analytics.aplicacao.ConsultarClassificacao;
import com.futebol.futebol_analytics.aplicacao.ConsultarPartidas;
import com.futebol.futebol_analytics.aplicacao.ConsultarResumo;
import com.futebol.futebol_analytics.aplicacao.porta.FonteDePartidas;
import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.estatistica.CalculadoraDeClassificacao;
import com.futebol.futebol_analytics.estatistica.CalculadoraDeResumo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raiz de composição: é aqui que os casos de uso recebem seus adaptadores.
 * Domínio e aplicação não conhecem o Spring; só esta classe os liga a ele.
 */
@Configuration
class CasosDeUsoConfig {

    @Bean
    ColetarPartidas coletarPartidas(FonteDePartidas fonte, RepositorioDePartidas repositorio) {
        return new ColetarPartidas(fonte, repositorio);
    }

    @Bean
    ConsultarClassificacao consultarClassificacao(RepositorioDePartidas repositorio) {
        return new ConsultarClassificacao(repositorio, new CalculadoraDeClassificacao());
    }

    @Bean
    ConsultarResumo consultarResumo(RepositorioDePartidas repositorio) {
        return new ConsultarResumo(repositorio, new CalculadoraDeResumo());
    }

    @Bean
    ConsultarPartidas consultarPartidas(RepositorioDePartidas repositorio) {
        return new ConsultarPartidas(repositorio);
    }
}
