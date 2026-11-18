package com.futebol.futebol_analytics.previsao;

import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvaliadorDeModeloTest {

    /** Modelo de mentira que sempre dá 100% para o mandante: o resultado da avaliação fica previsível. */
    private static final TreinadorDeModelo SEMPRE_MANDANTE = historico ->
            partida -> new Previsao(partida, 1, 0, 1.0, 0.0, 0.0, new Placar(1, 0));

    @Test
    @DisplayName("pula a primeira partida (sem passado para treinar) e ignora as não encerradas")
    void pulaPrimeiraPartida() {
        AvaliacaoDoModelo avaliacao = new AvaliadorDeModelo(SEMPRE_MANDANTE).avaliar(List.of(
                encerrada("Aurora", "Serrano", 1, 0),
                encerrada("Litoral", "Porto Real", 2, 0),
                encerrada("Serrano", "Litoral", 0, 1),
                agendada("Aurora", "Litoral")));

        assertEquals(2, avaliacao.partidasAvaliadas());
        assertEquals(1, avaliacao.acertos());
        assertEquals(1, avaliacao.acertosDaLinhaDeBase());
        // acerto certeiro: Brier 0; erro certeiro (1,0,0 contra 0,0,1): 1 + 0 + 1 = 2. Média: 1.
        assertEquals(1.0, avaliacao.brierScore(), 1e-9);
    }

    @Test
    @DisplayName("treina só com partidas anteriores à que está sendo prevista")
    void naoVazaODaFuturo() {
        List<Integer> tamanhosDoTreino = new ArrayList<>();
        TreinadorDeModelo espiao = historico -> {
            tamanhosDoTreino.add(historico.size());
            return SEMPRE_MANDANTE.treinar(historico);
        };
        List<Partida> partidas = List.of(
                encerrada("Aurora", "Serrano", 1, 0),
                encerrada("Litoral", "Porto Real", 2, 0),
                encerrada("Serrano", "Litoral", 0, 1));

        new AvaliadorDeModelo(espiao).avaliar(partidas);

        assertEquals(List.of(1, 2), tamanhosDoTreino);
    }

    @Test
    @DisplayName("modelo Poisson na liga de demonstração produz métricas dentro dos limites")
    void metricasDentroDosLimites() {
        List<Partida> partidas = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            partidas.add(encerrada("Aurora", "Serrano", 2, 0));
            partidas.add(encerrada("Serrano", "Aurora", 0, 1));
        }

        AvaliacaoDoModelo avaliacao = new AvaliadorDeModelo(new TreinadorPoisson()).avaliar(partidas);

        assertEquals(11, avaliacao.partidasAvaliadas());
        assertTrue(avaliacao.brierScore() >= 0 && avaliacao.brierScore() <= 2);
        assertTrue(avaliacao.taxaDeAcerto() > 0.5, "um padrão tão repetido deveria ser aprendido");
    }
}
