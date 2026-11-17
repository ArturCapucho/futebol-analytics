package com.futebol.futebol_analytics.aplicacao;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.estatistica.CalculadoraDeClassificacao;
import com.futebol.futebol_analytics.estatistica.LinhaDaClassificacao;
import java.util.List;
import java.util.Objects;

public final class ConsultarClassificacao {

    private final RepositorioDePartidas repositorio;
    private final CalculadoraDeClassificacao calculadora;

    public ConsultarClassificacao(RepositorioDePartidas repositorio, CalculadoraDeClassificacao calculadora) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.calculadora = Objects.requireNonNull(calculadora);
    }

    public List<LinhaDaClassificacao> executar(Competicao competicao) {
        return calculadora.calcular(repositorio.buscarPor(competicao));
    }
}
