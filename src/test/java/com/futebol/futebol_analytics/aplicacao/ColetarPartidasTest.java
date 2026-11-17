package com.futebol.futebol_analytics.aplicacao;

import static com.futebol.futebol_analytics.Partidas.COMPETICAO;
import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.futebol.futebol_analytics.Partidas.RepositorioEmMemoria;
import com.futebol.futebol_analytics.dominio.Partida;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ColetarPartidasTest {

    @Test
    @DisplayName("grava no repositório o que a fonte devolveu e informa a quantidade")
    void gravaPartidasDaFonte() {
        List<Partida> daFonte = List.of(encerrada("Aurora", "Serrano", 1, 0), agendada("Serrano", "Aurora"));
        RepositorioEmMemoria repositorio = new RepositorioEmMemoria();
        ColetarPartidas coletar = new ColetarPartidas(competicao -> daFonte, repositorio);

        ResultadoDaColeta resultado = coletar.executar(COMPETICAO);

        assertEquals(new ResultadoDaColeta(COMPETICAO, 2), resultado);
        assertEquals(daFonte, repositorio.buscarPor(COMPETICAO));
    }

    @Test
    @DisplayName("coletar duas vezes não duplica partidas")
    void coletaEhIdempotente() {
        List<Partida> daFonte = List.of(encerrada("Aurora", "Serrano", 1, 0));
        RepositorioEmMemoria repositorio = new RepositorioEmMemoria();
        ColetarPartidas coletar = new ColetarPartidas(competicao -> daFonte, repositorio);

        coletar.executar(COMPETICAO);
        coletar.executar(COMPETICAO);

        assertEquals(1, repositorio.buscarPor(COMPETICAO).size());
    }

    @Test
    @DisplayName("falha da fonte é propagada e nada é gravado")
    void propagaFalhaDaFonte() {
        RepositorioEmMemoria repositorio = new RepositorioEmMemoria();
        ColetarPartidas coletar = new ColetarPartidas(
                competicao -> {
                    throw new FalhaNaColetaException("fonte fora do ar");
                },
                repositorio);

        assertThrows(FalhaNaColetaException.class, () -> coletar.executar(COMPETICAO));
        assertTrue(repositorio.buscarPor(COMPETICAO).isEmpty());
    }
}
