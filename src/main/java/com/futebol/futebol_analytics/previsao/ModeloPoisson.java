package com.futebol.futebol_analytics.previsao;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.Time;
import java.util.Map;

/**
 * Modelo de Poisson para gols, criado por {@link TreinadorPoisson}.
 *
 * <p>Ideia: os gols de cada time em uma partida seguem uma distribuição de
 * Poisson. A média esperada (lambda) do mandante é
 * {@code média da liga em casa × ataque em casa do mandante × defesa fora do visitante},
 * e a do visitante é a simétrica. Com as duas distribuições, calcula-se a
 * probabilidade de cada placar de 0 x 0 até 10 x 10 e somam-se as de cada resultado.
 */
final class ModeloPoisson implements ModeloDePrevisao {

    /** Placares acima disso têm probabilidade desprezível; o que sobra é redistribuído na normalização. */
    private static final int MAXIMO_DE_GOLS = 10;

    private final double mediaGolsMandante;
    private final double mediaGolsVisitante;
    private final Map<Time, Forca> forcas;

    ModeloPoisson(double mediaGolsMandante, double mediaGolsVisitante, Map<Time, Forca> forcas) {
        this.mediaGolsMandante = mediaGolsMandante;
        this.mediaGolsVisitante = mediaGolsVisitante;
        this.forcas = Map.copyOf(forcas);
    }

    @Override
    public Previsao prever(Partida partida) {
        Forca mandante = forcas.getOrDefault(partida.mandante(), Forca.NEUTRA);
        Forca visitante = forcas.getOrDefault(partida.visitante(), Forca.NEUTRA);

        double lambdaMandante = mediaGolsMandante * mandante.ataqueEmCasa() * visitante.defesaFora();
        double lambdaVisitante = mediaGolsVisitante * visitante.ataqueFora() * mandante.defesaEmCasa();

        double[] golsMandante = distribuicaoPoisson(lambdaMandante);
        double[] golsVisitante = distribuicaoPoisson(lambdaVisitante);

        double vitoriaMandante = 0;
        double empate = 0;
        double vitoriaVisitante = 0;
        double maiorChance = -1;
        Placar placarMaisProvavel = new Placar(0, 0);
        for (int gm = 0; gm <= MAXIMO_DE_GOLS; gm++) {
            for (int gv = 0; gv <= MAXIMO_DE_GOLS; gv++) {
                // Os gols de um time são tratados como independentes dos gols do outro.
                double chance = golsMandante[gm] * golsVisitante[gv];
                if (gm > gv) {
                    vitoriaMandante += chance;
                } else if (gm == gv) {
                    empate += chance;
                } else {
                    vitoriaVisitante += chance;
                }
                if (chance > maiorChance) {
                    maiorChance = chance;
                    placarMaisProvavel = new Placar(gm, gv);
                }
            }
        }
        double total = vitoriaMandante + empate + vitoriaVisitante;
        return new Previsao(
                partida,
                lambdaMandante,
                lambdaVisitante,
                vitoriaMandante / total,
                empate / total,
                1.0 - (vitoriaMandante + empate) / total, // evita que o arredondamento quebre a soma 1
                placarMaisProvavel);
    }

    /** P(k) = e^-λ · λ^k / k!, calculada de forma incremental para não estourar o fatorial. */
    static double[] distribuicaoPoisson(double lambda) {
        double[] probabilidades = new double[MAXIMO_DE_GOLS + 1];
        probabilidades[0] = Math.exp(-lambda);
        for (int k = 1; k <= MAXIMO_DE_GOLS; k++) {
            probabilidades[k] = probabilidades[k - 1] * lambda / k;
        }
        return probabilidades;
    }

    /**
     * Força relativa de um time; 1.0 é exatamente a média da liga.
     * Ataque acima de 1 marca mais que a média; defesa acima de 1 sofre mais que a média.
     */
    record Forca(double ataqueEmCasa, double defesaEmCasa, double ataqueFora, double defesaFora) {

        static final Forca NEUTRA = new Forca(1.0, 1.0, 1.0, 1.0);
    }
}
