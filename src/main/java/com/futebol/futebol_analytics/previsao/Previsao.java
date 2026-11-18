package com.futebol.futebol_analytics.previsao;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.Resultado;
import java.util.Objects;

/**
 * O que um modelo espera de uma partida.
 *
 * <p>As três chances somam 1. Atenção: o {@code placarMaisProvavel} e o
 * {@link #resultadoMaisProvavel()} podem discordar. Um 1 x 1 pode ser o placar
 * individual mais provável e, ainda assim, a soma de todos os placares de
 * vitória do mandante (1 x 0, 2 x 0, 2 x 1...) ser maior que a soma dos empates.
 */
public record Previsao(
        Partida partida,
        double golsEsperadosMandante,
        double golsEsperadosVisitante,
        double chanceVitoriaMandante,
        double chanceEmpate,
        double chanceVitoriaVisitante,
        Placar placarMaisProvavel) {

    private static final double TOLERANCIA = 1e-6;

    public Previsao {
        Objects.requireNonNull(partida, "partida é obrigatória");
        Objects.requireNonNull(placarMaisProvavel, "placar mais provável é obrigatório");
        double soma = chanceVitoriaMandante + chanceEmpate + chanceVitoriaVisitante;
        if (Math.abs(soma - 1.0) > TOLERANCIA) {
            throw new IllegalArgumentException("as chances precisam somar 1, somaram " + soma);
        }
    }

    public Resultado resultadoMaisProvavel() {
        if (chanceVitoriaMandante >= chanceEmpate && chanceVitoriaMandante >= chanceVitoriaVisitante) {
            return Resultado.VITORIA_MANDANTE;
        }
        if (chanceVitoriaVisitante >= chanceEmpate) {
            return Resultado.VITORIA_VISITANTE;
        }
        return Resultado.EMPATE;
    }

    public double chanceDe(Resultado resultado) {
        return switch (resultado) {
            case VITORIA_MANDANTE -> chanceVitoriaMandante;
            case EMPATE -> chanceEmpate;
            case VITORIA_VISITANTE -> chanceVitoriaVisitante;
        };
    }
}
