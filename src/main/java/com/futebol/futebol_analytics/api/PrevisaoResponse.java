package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.previsao.Previsao;

/** Chances em percentual (0 a 100); gols esperados com duas casas. */
record PrevisaoResponse(
        String id,
        int rodada,
        String dataHora,
        String mandante,
        String visitante,
        double golsEsperadosMandante,
        double golsEsperadosVisitante,
        double chanceVitoriaMandante,
        double chanceEmpate,
        double chanceVitoriaVisitante,
        String placarMaisProvavel,
        String resultadoMaisProvavel) {

    static PrevisaoResponse de(Previsao previsao) {
        Partida partida = previsao.partida();
        Placar placar = previsao.placarMaisProvavel();
        return new PrevisaoResponse(
                partida.idExterno(),
                partida.rodada(),
                partida.dataHora().toString(),
                partida.mandante().nome(),
                partida.visitante().nome(),
                Numeros.arredondar(previsao.golsEsperadosMandante(), 2),
                Numeros.arredondar(previsao.golsEsperadosVisitante(), 2),
                Numeros.percentual(previsao.chanceVitoriaMandante()),
                Numeros.percentual(previsao.chanceEmpate()),
                Numeros.percentual(previsao.chanceVitoriaVisitante()),
                placar.golsMandante() + " x " + placar.golsVisitante(),
                previsao.resultadoMaisProvavel().name());
    }
}
