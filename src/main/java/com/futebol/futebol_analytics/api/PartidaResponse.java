package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;

/** {@code dataHora} vai como texto ISO-8601 em UTC; os gols são nulos enquanto a partida não termina. */
record PartidaResponse(
        String id,
        int rodada,
        String dataHora,
        String mandante,
        String visitante,
        String status,
        Integer golsMandante,
        Integer golsVisitante) {

    static PartidaResponse de(Partida partida) {
        Placar placar = partida.placar();
        return new PartidaResponse(
                partida.idExterno(),
                partida.rodada(),
                partida.dataHora().toString(),
                partida.mandante().nome(),
                partida.visitante().nome(),
                partida.status().name(),
                placar == null ? null : placar.golsMandante(),
                placar == null ? null : placar.golsVisitante());
    }
}
