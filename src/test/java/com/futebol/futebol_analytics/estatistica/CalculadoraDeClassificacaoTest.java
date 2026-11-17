package com.futebol.futebol_analytics.estatistica;

import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.cancelada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.futebol.futebol_analytics.dominio.Time;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraDeClassificacaoTest {

    private final CalculadoraDeClassificacao calculadora = new CalculadoraDeClassificacao();

    @Test
    @DisplayName("soma pontos, jogos e gols de mandante e visitante")
    void somaCampanha() {
        List<LinhaDaClassificacao> tabela = calculadora.calcular(List.of(
                encerrada("Aurora", "Serrano", 2, 0),
                encerrada("Serrano", "Aurora", 1, 1)));

        LinhaDaClassificacao aurora = tabela.get(0);
        assertEquals(new Time("Aurora"), aurora.time());
        assertEquals(4, aurora.pontos());
        assertEquals(2, aurora.jogos());
        assertEquals(1, aurora.vitorias());
        assertEquals(1, aurora.empates());
        assertEquals(0, aurora.derrotas());
        assertEquals(3, aurora.golsPro());
        assertEquals(1, aurora.golsContra());
        assertEquals(2, aurora.saldoDeGols());
        assertEquals(66.67, aurora.aproveitamento(), 0.01);

        LinhaDaClassificacao serrano = tabela.get(1);
        assertEquals(1, serrano.pontos());
        assertEquals(1, serrano.derrotas());
        assertEquals(-2, serrano.saldoDeGols());
    }

    @Test
    @DisplayName("desempata por vitórias, depois saldo, depois gols pró, depois nome")
    void aplicaCriteriosDeDesempate() {
        List<LinhaDaClassificacao> tabela = calculadora.calcular(List.of(
                // Aurora e Litoral: 3 pontos e 1 vitória cada; Aurora tem saldo melhor
                encerrada("Aurora", "Zeta", 3, 0),
                encerrada("Litoral", "Ypsilon", 1, 0),
                // Beta e Alfa: tudo igual, decide o nome
                encerrada("Beta", "Alfa", 2, 2)));

        assertEquals(
                List.of("Aurora", "Litoral", "Alfa", "Beta", "Ypsilon", "Zeta"),
                tabela.stream().map(linha -> linha.time().nome()).toList());
    }

    @Test
    @DisplayName("partida agendada lista os times zerados; cancelada é ignorada")
    void trataPartidasNaoEncerradas() {
        List<LinhaDaClassificacao> tabela = calculadora.calcular(List.of(
                agendada("Aurora", "Serrano"),
                cancelada("Litoral", "Porto Real")));

        assertEquals(2, tabela.size());
        assertTrue(tabela.stream().allMatch(linha -> linha.jogos() == 0 && linha.pontos() == 0));
        assertEquals(0.0, tabela.get(0).aproveitamento());
    }

    @Test
    @DisplayName("aceita outro critério de ordenação sem mudar a calculadora")
    void aceitaCriterioInjetado() {
        CalculadoraDeClassificacao porGolsPro = new CalculadoraDeClassificacao(
                Comparator.comparingInt(LinhaDaClassificacao::golsPro).reversed());

        List<LinhaDaClassificacao> tabela = porGolsPro.calcular(List.of(
                encerrada("Aurora", "Serrano", 1, 0),
                encerrada("Litoral", "Porto Real", 4, 5)));

        assertEquals("Porto Real", tabela.get(0).time().nome());
    }
}
