package com.futebol.futebol_analytics.previsao;

import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.Resultado;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TreinadorPoissonTest {

    private static final double PRECISAO = 1e-9;

    @Test
    @DisplayName("distribuição de Poisson confere com a fórmula e(-λ)·λ^k/k!")
    void distribuicaoConfereComFormula() {
        double[] p = ModeloPoisson.distribuicaoPoisson(1.5);

        assertEquals(Math.exp(-1.5), p[0], PRECISAO);
        assertEquals(Math.exp(-1.5) * 1.5, p[1], PRECISAO);
        assertEquals(Math.exp(-1.5) * 1.5 * 1.5 / 2, p[2], PRECISAO);
    }

    @Test
    @DisplayName("sem histórico, usa as médias típicas e as chances somam 1")
    void semHistoricoUsaMediasTipicas() {
        Previsao previsao = new TreinadorPoisson().treinar(List.of()).prever(agendada("Aurora", "Serrano"));

        assertEquals(TreinadorPoisson.MEDIA_PADRAO_GOLS_MANDANTE, previsao.golsEsperadosMandante(), PRECISAO);
        assertEquals(TreinadorPoisson.MEDIA_PADRAO_GOLS_VISITANTE, previsao.golsEsperadosVisitante(), PRECISAO);
        assertEquals(1.0, previsao.chanceVitoriaMandante() + previsao.chanceEmpate()
                + previsao.chanceVitoriaVisitante(), PRECISAO);
        assertEquals(Resultado.VITORIA_MANDANTE, previsao.resultadoMaisProvavel());
    }

    @Test
    @DisplayName("time que vence tudo é favorito contra time que perde tudo")
    void forteEhFavorito() {
        List<Partida> historico = new ArrayList<>();
        for (String adversario : List.of("Litoral", "Porto Real", "Estrela")) {
            historico.add(encerrada("Aurora", adversario, 3, 0));
            historico.add(encerrada(adversario, "Aurora", 0, 2));
            historico.add(encerrada("Serrano", adversario, 0, 2));
            historico.add(encerrada(adversario, "Serrano", 3, 0));
        }
        ModeloDePrevisao modelo = new TreinadorPoisson().treinar(historico);

        Previsao forteEmCasa = modelo.prever(agendada("Aurora", "Serrano"));
        Previsao forteFora = modelo.prever(agendada("Serrano", "Aurora"));

        assertTrue(forteEmCasa.chanceVitoriaMandante() > 0.6, "chance: " + forteEmCasa.chanceVitoriaMandante());
        assertTrue(forteEmCasa.golsEsperadosMandante() > forteEmCasa.golsEsperadosVisitante());
        assertEquals(Resultado.VITORIA_VISITANTE, forteFora.resultadoMaisProvavel());
    }

    @Test
    @DisplayName("regularização: uma goleada isolada não transforma o time em imbatível")
    void regularizacaoSegura() {
        List<Partida> historico = List.of(
                encerrada("Aurora", "Serrano", 6, 0),
                encerrada("Litoral", "Porto Real", 1, 1),
                encerrada("Porto Real", "Litoral", 1, 1));

        // O adversário precisa ser um time sem jogos (força neutra). Contra o Litoral, a
        // regularização também puxaria a defesa dele para a média e o efeito se misturaria.
        double semRegularizacao = new TreinadorPoisson(0).treinar(historico)
                .prever(agendada("Aurora", "Estreante")).golsEsperadosMandante();
        double comRegularizacao = new TreinadorPoisson(2).treinar(historico)
                .prever(agendada("Aurora", "Estreante")).golsEsperadosMandante();

        assertTrue(comRegularizacao < semRegularizacao,
                "com " + comRegularizacao + " deveria ser menor que sem " + semRegularizacao);
    }

    @Test
    @DisplayName("placar mais provável é o de maior probabilidade individual")
    void placarMaisProvavel() {
        Previsao previsao = new TreinadorPoisson().treinar(List.of()).prever(agendada("Aurora", "Serrano"));

        // λ = 1,5 e 1,1: P(1 gol) é a maior das duas distribuições, logo 1 x 1.
        assertEquals(new Placar(1, 1), previsao.placarMaisProvavel());
    }
}
