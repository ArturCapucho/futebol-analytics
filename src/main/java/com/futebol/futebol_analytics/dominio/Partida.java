package com.futebol.futebol_analytics.dominio;

import java.time.Instant;
import java.util.Objects;

/**
 * Uma partida de uma competição.
 *
 * <p>Invariantes garantidas no construtor:
 * <ul>
 *   <li>um time não joga contra si mesmo;</li>
 *   <li>partida {@link StatusPartida#ENCERRADA} sempre tem placar;</li>
 *   <li>partida em qualquer outro status nunca tem placar ({@code placar} é {@code null}).</li>
 * </ul>
 *
 * @param idExterno identificador da partida na fonte de dados; é o que torna a coleta idempotente
 * @param rodada    número da rodada, ou 0 quando a competição não é organizada em rodadas
 * @param placar    placar final; {@code null} enquanto a partida não está encerrada
 */
public record Partida(
        String idExterno,
        Competicao competicao,
        int rodada,
        Instant dataHora,
        Time mandante,
        Time visitante,
        StatusPartida status,
        Placar placar) {

    public Partida {
        Objects.requireNonNull(idExterno, "idExterno é obrigatório");
        Objects.requireNonNull(competicao, "competição é obrigatória");
        Objects.requireNonNull(dataHora, "data e hora são obrigatórias");
        Objects.requireNonNull(mandante, "mandante é obrigatório");
        Objects.requireNonNull(visitante, "visitante é obrigatório");
        Objects.requireNonNull(status, "status é obrigatório");
        if (idExterno.isBlank()) {
            throw new IllegalArgumentException("idExterno não pode ser vazio");
        }
        if (rodada < 0) {
            throw new IllegalArgumentException("rodada não pode ser negativa: " + rodada);
        }
        if (mandante.equals(visitante)) {
            throw new IllegalArgumentException("um time não joga contra si mesmo: " + mandante.nome());
        }
        if (status == StatusPartida.ENCERRADA && placar == null) {
            throw new IllegalArgumentException("partida encerrada precisa de placar: " + idExterno);
        }
        if (status != StatusPartida.ENCERRADA && placar != null) {
            throw new IllegalArgumentException("só partida encerrada tem placar: " + idExterno);
        }
    }

    public boolean encerrada() {
        return status == StatusPartida.ENCERRADA;
    }
}
