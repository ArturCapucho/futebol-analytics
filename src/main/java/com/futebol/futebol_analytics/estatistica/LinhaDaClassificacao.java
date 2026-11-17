package com.futebol.futebol_analytics.estatistica;

import com.futebol.futebol_analytics.dominio.Time;

/** Campanha de um time na tabela. Imutável: cada jogo somado gera uma nova linha. */
public record LinhaDaClassificacao(
        Time time, int jogos, int vitorias, int empates, int derrotas, int golsPro, int golsContra) {

    private static final int PONTOS_POR_VITORIA = 3;

    public static LinhaDaClassificacao vazia(Time time) {
        return new LinhaDaClassificacao(time, 0, 0, 0, 0, 0, 0);
    }

    public LinhaDaClassificacao comJogo(int golsFeitos, int golsSofridos) {
        return new LinhaDaClassificacao(
                time,
                jogos + 1,
                vitorias + (golsFeitos > golsSofridos ? 1 : 0),
                empates + (golsFeitos == golsSofridos ? 1 : 0),
                derrotas + (golsFeitos < golsSofridos ? 1 : 0),
                golsPro + golsFeitos,
                golsContra + golsSofridos);
    }

    public int pontos() {
        return vitorias * PONTOS_POR_VITORIA + empates;
    }

    public int saldoDeGols() {
        return golsPro - golsContra;
    }

    /** Percentual dos pontos disputados que o time conquistou (0 a 100). */
    public double aproveitamento() {
        if (jogos == 0) {
            return 0.0;
        }
        return 100.0 * pontos() / (jogos * PONTOS_POR_VITORIA);
    }
}
