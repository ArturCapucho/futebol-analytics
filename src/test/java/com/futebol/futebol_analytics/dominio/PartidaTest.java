package com.futebol.futebol_analytics.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PartidaTest {

    private static final Competicao COMPETICAO = new Competicao("bsa", 2026);
    private static final Instant DATA = Instant.parse("2026-05-10T19:00:00Z");
    private static final Time AURORA = new Time("Aurora");
    private static final Time SERRANO = new Time("Serrano");

    @Test
    @DisplayName("placar informa o resultado pelo ponto de vista do mandante")
    void placarInformaResultado() {
        assertEquals(Resultado.VITORIA_MANDANTE, new Placar(2, 1).resultado());
        assertEquals(Resultado.EMPATE, new Placar(0, 0).resultado());
        assertEquals(Resultado.VITORIA_VISITANTE, new Placar(1, 3).resultado());
        assertEquals(4, new Placar(1, 3).totalDeGols());
    }

    @Test
    @DisplayName("placar não aceita gols negativos")
    void placarRejeitaGolsNegativos() {
        assertThrows(IllegalArgumentException.class, () -> new Placar(-1, 0));
    }

    @Test
    @DisplayName("competição normaliza o código e time normaliza o nome")
    void normalizaTextos() {
        assertEquals("BSA", COMPETICAO.codigo());
        assertEquals(new Time("Aurora"), new Time("  Aurora "));
        assertThrows(IllegalArgumentException.class, () -> new Time("   "));
    }

    @Test
    @DisplayName("partida encerrada exige placar")
    void encerradaExigePlacar() {
        assertThrows(IllegalArgumentException.class,
                () -> new Partida("p1", COMPETICAO, 1, DATA, AURORA, SERRANO, StatusPartida.ENCERRADA, null));
    }

    @Test
    @DisplayName("partida não encerrada não aceita placar")
    void naoEncerradaRejeitaPlacar() {
        assertThrows(IllegalArgumentException.class,
                () -> new Partida("p1", COMPETICAO, 1, DATA, AURORA, SERRANO, StatusPartida.AGENDADA, new Placar(1, 0)));
    }

    @Test
    @DisplayName("um time não joga contra si mesmo")
    void rejeitaTimeContraSiMesmo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Partida("p1", COMPETICAO, 1, DATA, AURORA, AURORA, StatusPartida.AGENDADA, null));
    }

    @Test
    @DisplayName("encerrada() reflete o status")
    void encerradaRefleteStatus() {
        Partida encerrada = new Partida("p1", COMPETICAO, 1, DATA, AURORA, SERRANO,
                StatusPartida.ENCERRADA, new Placar(1, 0));
        Partida agendada = new Partida("p2", COMPETICAO, 2, DATA, SERRANO, AURORA, StatusPartida.AGENDADA, null);

        assertTrue(encerrada.encerrada());
        assertFalse(agendada.encerrada());
    }
}
