package com.futebol.futebol_analytics.aplicacao;

import com.futebol.futebol_analytics.aplicacao.porta.FonteDePartidas;
import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: trazer as partidas de uma competição da fonte para o repositório.
 * Pode ser executado quantas vezes for preciso; o repositório casa por idExterno.
 */
public final class ColetarPartidas {

    private final FonteDePartidas fonte;
    private final RepositorioDePartidas repositorio;

    public ColetarPartidas(FonteDePartidas fonte, RepositorioDePartidas repositorio) {
        this.fonte = Objects.requireNonNull(fonte);
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public ResultadoDaColeta executar(Competicao competicao) {
        List<Partida> partidas = fonte.buscar(competicao);
        repositorio.salvarTodas(partidas);
        return new ResultadoDaColeta(competicao, partidas.size());
    }
}
