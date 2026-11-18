package com.futebol.futebol_analytics.aplicacao;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.estatistica.CalculadoraDeDesempenho;
import com.futebol.futebol_analytics.estatistica.DesempenhoDoTime;
import java.util.List;
import java.util.Objects;

public final class ConsultarDesempenho {

    private final RepositorioDePartidas repositorio;
    private final CalculadoraDeDesempenho calculadora;

    public ConsultarDesempenho(RepositorioDePartidas repositorio, CalculadoraDeDesempenho calculadora) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.calculadora = Objects.requireNonNull(calculadora);
    }

    public List<DesempenhoDoTime> executar(Competicao competicao) {
        return calculadora.calcular(repositorio.buscarPor(competicao));
    }
}
