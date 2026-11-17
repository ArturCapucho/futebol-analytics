package com.futebol.futebol_analytics.infraestrutura.coleta;

import com.futebol.futebol_analytics.aplicacao.FalhaNaColetaException;
import com.futebol.futebol_analytics.aplicacao.porta.FonteDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import com.futebol.futebol_analytics.dominio.Time;
import java.time.Instant;
import java.util.List;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Busca partidas na API v4 do football-data.org.
 *
 * <p>Camada anticorrupção: os nomes e formatos da API (em inglês) ficam
 * confinados aos records internos desta classe e são traduzidos para o
 * domínio em {@link #paraDominio}.
 */
public final class FonteFootballData implements FonteDePartidas {

    private static final String PREFIXO_ID = "football-data:";

    private final RestClient http;

    public FonteFootballData(RestClient http) {
        this.http = http;
    }

    @Override
    public List<Partida> buscar(Competicao competicao) {
        RespostaDePartidas resposta;
        try {
            resposta = http.get()
                    .uri("/competitions/{codigo}/matches?season={temporada}",
                            competicao.codigo(), competicao.temporada())
                    .retrieve()
                    .body(RespostaDePartidas.class);
        } catch (RestClientException e) {
            throw new FalhaNaColetaException(
                    "Falha ao consultar o football-data.org para " + competicao.codigo(), e);
        }
        if (resposta == null || resposta.matches() == null) {
            throw new FalhaNaColetaException("Resposta vazia do football-data.org para " + competicao.codigo());
        }
        try {
            return resposta.matches().stream().map(match -> paraDominio(match, competicao)).toList();
        } catch (RuntimeException e) {
            throw new FalhaNaColetaException("Resposta inesperada do football-data.org: " + e.getMessage(), e);
        }
    }

    static Partida paraDominio(Match match, Competicao competicao) {
        StatusPartida status = traduzirStatus(match.status());
        Placar placar = null;
        if (status == StatusPartida.ENCERRADA) {
            Gols gols = match.score().fullTime();
            placar = new Placar(gols.home(), gols.away());
        }
        return new Partida(
                PREFIXO_ID + match.id(),
                competicao,
                match.matchday() == null ? 0 : match.matchday(),
                Instant.parse(match.utcDate()),
                new Time(match.homeTeam().name()),
                new Time(match.awayTeam().name()),
                status,
                placar);
    }

    private static StatusPartida traduzirStatus(String status) {
        return switch (status) {
            case "FINISHED", "AWARDED" -> StatusPartida.ENCERRADA;
            case "IN_PLAY", "PAUSED", "LIVE" -> StatusPartida.EM_ANDAMENTO;
            case "POSTPONED", "SUSPENDED" -> StatusPartida.ADIADA;
            case "CANCELLED" -> StatusPartida.CANCELADA;
            default -> StatusPartida.AGENDADA;
        };
    }

    record RespostaDePartidas(List<Match> matches) {
    }

    record Match(long id, String utcDate, String status, Integer matchday,
                 Equipe homeTeam, Equipe awayTeam, Score score) {
    }

    record Equipe(String name) {
    }

    record Score(Gols fullTime) {
    }

    record Gols(Integer home, Integer away) {
    }
}
