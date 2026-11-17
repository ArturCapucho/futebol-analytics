package com.futebol.futebol_analytics.dominio;

/** Placar final de uma partida. */
public record Placar(int golsMandante, int golsVisitante) {

    public Placar {
        if (golsMandante < 0 || golsVisitante < 0) {
            throw new IllegalArgumentException(
                    "gols não podem ser negativos: " + golsMandante + " x " + golsVisitante);
        }
    }

    public Resultado resultado() {
        if (golsMandante > golsVisitante) {
            return Resultado.VITORIA_MANDANTE;
        }
        if (golsMandante < golsVisitante) {
            return Resultado.VITORIA_VISITANTE;
        }
        return Resultado.EMPATE;
    }

    public int totalDeGols() {
        return golsMandante + golsVisitante;
    }
}
