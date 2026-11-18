package com.futebol.futebol_analytics.aplicacao;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.previsao.AvaliacaoDoModelo;
import com.futebol.futebol_analytics.previsao.AvaliadorDeModelo;
import java.util.Objects;

public final class AvaliarModelo {

    private final RepositorioDePartidas repositorio;
    private final AvaliadorDeModelo avaliador;

    public AvaliarModelo(RepositorioDePartidas repositorio, AvaliadorDeModelo avaliador) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.avaliador = Objects.requireNonNull(avaliador);
    }

    public AvaliacaoDoModelo executar(Competicao competicao) {
        return avaliador.avaliar(repositorio.buscarPor(competicao));
    }
}
