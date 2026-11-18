package com.futebol.futebol_analytics.estatistica;

import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static com.futebol.futebol_analytics.estatistica.ResultadoDoTime.DERROTA;
import static com.futebol.futebol_analytics.estatistica.ResultadoDoTime.EMPATE;
import static com.futebol.futebol_analytics.estatistica.ResultadoDoTime.VITORIA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.futebol.futebol_analytics.dominio.Partida;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraDeDesempenhoTest {

    @Test
    @DisplayName("separa a campanha em casa da campanha fora")
    void separaCasaEFora() {
        List<DesempenhoDoTime> desempenhos = new CalculadoraDeDesempenho().calcular(List.of(
                encerrada("Aurora", "Serrano", 3, 0),
                encerrada("Serrano", "Aurora", 2, 1),
                agendada("Aurora", "Serrano")));

        DesempenhoDoTime aurora = desempenhos.get(0);
        assertEquals("Aurora", aurora.time().nome());
        assertEquals(1, aurora.emCasa().vitorias());
        assertEquals(3, aurora.emCasa().golsPro());
        assertEquals(1, aurora.foraDeCasa().derrotas());
        assertEquals(1, aurora.foraDeCasa().golsPro());
        assertEquals(List.of(VITORIA, DERROTA), aurora.ultimosResultados());
    }

    @Test
    @DisplayName("guarda só os últimos jogos, em ordem cronológica, mesmo recebendo fora de ordem")
    void mantemSoOsUltimos() {
        Partida primeira = encerrada("Aurora", "Serrano", 0, 1);
        Partida segunda = encerrada("Aurora", "Serrano", 1, 1);
        Partida terceira = encerrada("Aurora", "Serrano", 2, 0);
        List<Partida> foraDeOrdem = new ArrayList<>(List.of(terceira, primeira, segunda));

        DesempenhoDoTime aurora = new CalculadoraDeDesempenho(2).calcular(foraDeOrdem).get(0);

        assertEquals(List.of(EMPATE, VITORIA), aurora.ultimosResultados());
    }

    @Test
    @DisplayName("tamanho da sequência precisa ser positivo")
    void rejeitaSequenciaVazia() {
        assertThrows(IllegalArgumentException.class, () -> new CalculadoraDeDesempenho(0));
    }
}
