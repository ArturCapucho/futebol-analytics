package com.futebol.futebol_analytics.aplicacao;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import java.util.List;
import java.util.Objects;

public final class ConsultarPartidas {

    private final RepositorioDePartidas repositorio;

    public ConsultarPartidas(RepositorioDePartidas repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public List<Partida> executar(Competicao competicao) {
        return repositorio.buscarPor(competicao);
    }
}
