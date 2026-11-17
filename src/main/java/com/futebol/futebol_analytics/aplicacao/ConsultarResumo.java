package com.futebol.futebol_analytics.aplicacao;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.estatistica.CalculadoraDeResumo;
import com.futebol.futebol_analytics.estatistica.ResumoDaCompeticao;
import java.util.Objects;

public final class ConsultarResumo {

    private final RepositorioDePartidas repositorio;
    private final CalculadoraDeResumo calculadora;

    public ConsultarResumo(RepositorioDePartidas repositorio, CalculadoraDeResumo calculadora) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.calculadora = Objects.requireNonNull(calculadora);
    }

    public ResumoDaCompeticao executar(Competicao competicao) {
        return calculadora.calcular(repositorio.buscarPor(competicao));
    }
}
