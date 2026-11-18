package com.futebol.futebol_analytics.estatistica;

/** Resultado de uma partida do ponto de vista de um dos times (diferente de {@code Resultado}, que é do ponto de vista do mando). */
public enum ResultadoDoTime {
    VITORIA,
    EMPATE,
    DERROTA;

    static ResultadoDoTime de(int golsFeitos, int golsSofridos) {
        if (golsFeitos > golsSofridos) {
            return VITORIA;
        }
        if (golsFeitos < golsSofridos) {
            return DERROTA;
        }
        return EMPATE;
    }
}
