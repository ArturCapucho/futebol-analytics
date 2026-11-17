package com.futebol.futebol_analytics.estatistica;

/** Números gerais de uma competição, considerando só partidas encerradas. */
public record ResumoDaCompeticao(
        int partidasEncerradas,
        int totalDeGols,
        int vitoriasDoMandante,
        int empates,
        int vitoriasDoVisitante) {

    public double mediaDeGolsPorPartida() {
        if (partidasEncerradas == 0) {
            return 0.0;
        }
        return (double) totalDeGols / partidasEncerradas;
    }
}
