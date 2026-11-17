package com.futebol.futebol_analytics.estatistica;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import com.futebol.futebol_analytics.dominio.Time;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Monta a tabela de classificação a partir das partidas.
 *
 * <p>O critério de desempate é injetado: cada campeonato tem o seu, e trocar
 * o critério não exige alterar esta classe.
 */
public final class CalculadoraDeClassificacao {

    /** Pontos, vitórias, saldo de gols, gols pró e, persistindo o empate, ordem alfabética. */
    public static final Comparator<LinhaDaClassificacao> CRITERIO_PADRAO =
            Comparator.comparingInt(LinhaDaClassificacao::pontos)
                    .thenComparingInt(LinhaDaClassificacao::vitorias)
                    .thenComparingInt(LinhaDaClassificacao::saldoDeGols)
                    .thenComparingInt(LinhaDaClassificacao::golsPro)
                    .reversed()
                    .thenComparing(linha -> linha.time().nome());

    private final Comparator<LinhaDaClassificacao> criterio;

    public CalculadoraDeClassificacao() {
        this(CRITERIO_PADRAO);
    }

    public CalculadoraDeClassificacao(Comparator<LinhaDaClassificacao> criterio) {
        this.criterio = Objects.requireNonNull(criterio, "critério é obrigatório");
    }

    /** Linhas ordenadas da primeira à última posição. Times sem jogo encerrado aparecem zerados. */
    public List<LinhaDaClassificacao> calcular(Collection<Partida> partidas) {
        Map<Time, LinhaDaClassificacao> linhas = new HashMap<>();
        for (Partida partida : partidas) {
            if (partida.status() == StatusPartida.CANCELADA) {
                continue;
            }
            linhas.putIfAbsent(partida.mandante(), LinhaDaClassificacao.vazia(partida.mandante()));
            linhas.putIfAbsent(partida.visitante(), LinhaDaClassificacao.vazia(partida.visitante()));
            if (partida.encerrada()) {
                Placar placar = partida.placar();
                linhas.computeIfPresent(partida.mandante(),
                        (time, linha) -> linha.comJogo(placar.golsMandante(), placar.golsVisitante()));
                linhas.computeIfPresent(partida.visitante(),
                        (time, linha) -> linha.comJogo(placar.golsVisitante(), placar.golsMandante()));
            }
        }
        return linhas.values().stream().sorted(criterio).toList();
    }
}
