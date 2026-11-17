package com.futebol.futebol_analytics.infraestrutura.coleta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import com.futebol.futebol_analytics.infraestrutura.coleta.FonteFootballData.Equipe;
import com.futebol.futebol_analytics.infraestrutura.coleta.FonteFootballData.Gols;
import com.futebol.futebol_analytics.infraestrutura.coleta.FonteFootballData.Match;
import com.futebol.futebol_analytics.infraestrutura.coleta.FonteFootballData.Score;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Testa a tradução do formato da API para o domínio, sem rede. */
class FonteFootballDataTest {

    private static final Competicao PREMIER = new Competicao("PL", 2025);

    @Test
    @DisplayName("partida FINISHED vira encerrada com o placar final")
    void traduzPartidaEncerrada() {
        Match match = new Match(497410, "2025-08-16T14:00:00Z", "FINISHED", 1,
                new Equipe("Arsenal FC"), new Equipe("Chelsea FC"), new Score(new Gols(2, 1)));

        Partida partida = FonteFootballData.paraDominio(match, PREMIER);

        assertEquals("football-data:497410", partida.idExterno());
        assertEquals(StatusPartida.ENCERRADA, partida.status());
        assertEquals(new Placar(2, 1), partida.placar());
        assertEquals("Arsenal FC", partida.mandante().nome());
        assertEquals(1, partida.rodada());
    }

    @Test
    @DisplayName("partida TIMED vira agendada, sem placar e com rodada 0 quando não há matchday")
    void traduzPartidaAgendada() {
        Match match = new Match(1, "2026-05-01T19:00:00Z", "TIMED", null,
                new Equipe("Arsenal FC"), new Equipe("Chelsea FC"), new Score(new Gols(null, null)));

        Partida partida = FonteFootballData.paraDominio(match, PREMIER);

        assertEquals(StatusPartida.AGENDADA, partida.status());
        assertNull(partida.placar());
        assertEquals(0, partida.rodada());
    }
}
