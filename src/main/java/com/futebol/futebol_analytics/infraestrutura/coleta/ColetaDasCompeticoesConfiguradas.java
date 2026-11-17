package com.futebol.futebol_analytics.infraestrutura.coleta;

import com.futebol.futebol_analytics.aplicacao.ColetarPartidas;
import com.futebol.futebol_analytics.aplicacao.FalhaNaColetaException;
import com.futebol.futebol_analytics.aplicacao.ResultadoDaColeta;
import com.futebol.futebol_analytics.infraestrutura.config.ColetaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Coleta todas as competições listadas na configuração. A falha de uma
 * competição é registrada e não impede a coleta das demais.
 */
@Component
class ColetaDasCompeticoesConfiguradas {

    private static final Logger log = LoggerFactory.getLogger(ColetaDasCompeticoesConfiguradas.class);

    private final ColetarPartidas coletarPartidas;
    private final ColetaProperties propriedades;

    ColetaDasCompeticoesConfiguradas(ColetarPartidas coletarPartidas, ColetaProperties propriedades) {
        this.coletarPartidas = coletarPartidas;
        this.propriedades = propriedades;
    }

    void executar() {
        for (ColetaProperties.CompeticaoConfigurada configurada : propriedades.competicoes()) {
            try {
                ResultadoDaColeta resultado = coletarPartidas.executar(configurada.paraDominio());
                log.info("Coleta de {}/{}: {} partidas",
                        configurada.codigo(), configurada.temporada(), resultado.partidasColetadas());
            } catch (FalhaNaColetaException e) {
                log.error("Coleta de {}/{} falhou", configurada.codigo(), configurada.temporada(), e);
            }
        }
    }
}
