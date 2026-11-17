package com.futebol.futebol_analytics.estatistica;

import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraDeResumoTest {

    private final CalculadoraDeResumo calculadora = new CalculadoraDeResumo();

    @Test
    @DisplayName("conta gols e resultados só das partidas encerradas")
    void resumeEncerradas() {
        ResumoDaCompeticao resumo = calculadora.calcular(List.of(
                encerrada("Aurora", "Serrano", 2, 0),
                encerrada("Litoral", "Porto Real", 1, 1),
                encerrada("Serrano", "Litoral", 0, 3),
                encerrada("Porto Real", "Aurora", 0, 1),
                agendada("Aurora", "Litoral")));

        assertEquals(new ResumoDaCompeticao(4, 8, 1, 1, 2), resumo);
        assertEquals(2.0, resumo.mediaDeGolsPorPartida());
    }

    @Test
    @DisplayName("sem partidas encerradas a média é zero")
    void semPartidas() {
        ResumoDaCompeticao resumo = calculadora.calcular(List.of());

        assertEquals(0, resumo.partidasEncerradas());
        assertEquals(0.0, resumo.mediaDeGolsPorPartida());
    }
}
