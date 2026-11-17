package com.futebol.futebol_analytics.infraestrutura.coleta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.futebol.futebol_analytics.aplicacao.FalhaNaColetaException;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

class FonteArquivoCsvTest {

    private static final String CABECALHO =
            "id;competicao;temporada;rodada;dataHora;mandante;visitante;golsMandante;golsVisitante\n";
    private static final Competicao DEMO = new Competicao("DEMO", 2026);

    @Test
    @DisplayName("lê o arquivo de demonstração que acompanha a aplicação")
    void leArquivoDeDemonstracao() {
        FonteArquivoCsv fonte = new FonteArquivoCsv(new ClassPathResource("dados/partidas-demo.csv"));

        List<Partida> partidas = fonte.buscar(DEMO);

        assertEquals(30, partidas.size());
        assertEquals(27, partidas.stream().filter(Partida::encerrada).count());
        assertEquals(new Placar(0, 1), partidas.get(0).placar());
    }

    @Test
    @DisplayName("gols em branco viram partida agendada, sem placar")
    void golsEmBrancoViramAgendada() {
        FonteArquivoCsv fonte = fonteCom("x1;DEMO;2026;3;2026-12-06T19:00:00Z;Aurora;Serrano;;\n");

        Partida partida = fonte.buscar(DEMO).get(0);

        assertEquals(StatusPartida.AGENDADA, partida.status());
        assertNull(partida.placar());
    }

    @Test
    @DisplayName("devolve só as partidas da competição e temporada pedidas")
    void filtraPorCompeticao() {
        FonteArquivoCsv fonte = fonteCom(
                "x1;DEMO;2026;1;2026-03-01T19:00:00Z;Aurora;Serrano;1;0\n"
                        + "x2;DEMO;2025;1;2025-03-01T19:00:00Z;Aurora;Serrano;1;0\n"
                        + "x3;OUTRA;2026;1;2026-03-01T19:00:00Z;Aurora;Serrano;1;0\n");

        assertEquals(List.of("x1"), fonte.buscar(DEMO).stream().map(Partida::idExterno).toList());
    }

    @Test
    @DisplayName("linha malformada vira falha de coleta com o número da linha")
    void linhaInvalidaViraFalha() {
        FonteArquivoCsv fonte = fonteCom("x1;DEMO;2026;1;data-errada;Aurora;Serrano;1;0\n");

        FalhaNaColetaException falha = assertThrows(FalhaNaColetaException.class, () -> fonte.buscar(DEMO));

        assertTrue(falha.getMessage().contains("Linha 2"));
    }

    private static FonteArquivoCsv fonteCom(String linhas) {
        return new FonteArquivoCsv(new ByteArrayResource((CABECALHO + linhas).getBytes(StandardCharsets.UTF_8)));
    }
}
