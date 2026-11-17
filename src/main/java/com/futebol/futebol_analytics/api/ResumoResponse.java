package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.estatistica.ResumoDaCompeticao;

record ResumoResponse(
        int partidasEncerradas,
        int totalDeGols,
        double mediaDeGolsPorPartida,
        int vitoriasDoMandante,
        int empates,
        int vitoriasDoVisitante) {

    static ResumoResponse de(ResumoDaCompeticao resumo) {
        return new ResumoResponse(
                resumo.partidasEncerradas(),
                resumo.totalDeGols(),
                Math.round(resumo.mediaDeGolsPorPartida() * 100.0) / 100.0,
                resumo.vitoriasDoMandante(),
                resumo.empates(),
                resumo.vitoriasDoVisitante());
    }
}
