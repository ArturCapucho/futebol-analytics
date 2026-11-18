package com.futebol.futebol_analytics.previsao;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Resultado;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Avalia um modelo "voltando no tempo" (backtest): para cada partida encerrada,
 * treina só com as partidas anteriores a ela, prevê e compara com o que aconteceu.
 *
 * <p>Treinar com a própria partida que se quer prever seria vazamento de dados:
 * o modelo pareceria ótimo aqui e falharia com jogos de verdade.
 */
public final class AvaliadorDeModelo {

    private final TreinadorDeModelo treinador;

    public AvaliadorDeModelo(TreinadorDeModelo treinador) {
        this.treinador = Objects.requireNonNull(treinador);
    }

    public AvaliacaoDoModelo avaliar(Collection<Partida> partidas) {
        List<Partida> encerradas = partidas.stream()
                .filter(Partida::encerrada)
                .sorted(Comparator.comparing(Partida::dataHora))
                .toList();

        int avaliadas = 0;
        int acertos = 0;
        int acertosDaLinhaDeBase = 0;
        double somaBrier = 0;
        for (Partida partida : encerradas) {
            List<Partida> passado = encerradas.stream()
                    .filter(anterior -> anterior.dataHora().isBefore(partida.dataHora()))
                    .toList();
            if (passado.isEmpty()) {
                continue;
            }
            Previsao previsao = treinador.treinar(passado).prever(partida);
            Resultado real = partida.placar().resultado();

            avaliadas++;
            if (previsao.resultadoMaisProvavel() == real) {
                acertos++;
            }
            if (real == Resultado.VITORIA_MANDANTE) {
                acertosDaLinhaDeBase++;
            }
            somaBrier += brier(previsao, real);
        }
        return new AvaliacaoDoModelo(
                avaliadas, acertos, acertosDaLinhaDeBase, avaliadas == 0 ? 0.0 : somaBrier / avaliadas);
    }

    /** Soma, nos três resultados possíveis, de (chance prevista − o que aconteceu)², onde aconteceu vale 1 ou 0. */
    private static double brier(Previsao previsao, Resultado real) {
        double soma = 0;
        for (Resultado possivel : Resultado.values()) {
            double aconteceu = possivel == real ? 1.0 : 0.0;
            double erro = previsao.chanceDe(possivel) - aconteceu;
            soma += erro * erro;
        }
        return soma;
    }
}
