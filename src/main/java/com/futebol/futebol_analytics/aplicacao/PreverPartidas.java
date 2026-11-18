package com.futebol.futebol_analytics.aplicacao;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import com.futebol.futebol_analytics.previsao.ModeloDePrevisao;
import com.futebol.futebol_analytics.previsao.Previsao;
import com.futebol.futebol_analytics.previsao.TreinadorDeModelo;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: prever as partidas agendadas de uma competição.
 *
 * <p>O modelo é treinado a cada chamada com todas as partidas encerradas da
 * competição. É barato com dezenas ou centenas de jogos; com volumes maiores,
 * o próximo passo seria treinar após cada coleta e guardar o modelo.
 */
public final class PreverPartidas {

    private final RepositorioDePartidas repositorio;
    private final TreinadorDeModelo treinador;

    public PreverPartidas(RepositorioDePartidas repositorio, TreinadorDeModelo treinador) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.treinador = Objects.requireNonNull(treinador);
    }

    public List<Previsao> executar(Competicao competicao) {
        List<Partida> partidas = repositorio.buscarPor(competicao);
        ModeloDePrevisao modelo = treinador.treinar(partidas);
        return partidas.stream()
                .filter(partida -> partida.status() == StatusPartida.AGENDADA)
                .map(modelo::prever)
                .toList();
    }
}
