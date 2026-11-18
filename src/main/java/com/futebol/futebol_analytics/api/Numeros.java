package com.futebol.futebol_analytics.api;

/** Arredondamento para exibição; os cálculos internos seguem com precisão total. */
final class Numeros {

    private Numeros() {
    }

    static double arredondar(double valor, int casas) {
        double fator = Math.pow(10, casas);
        return Math.round(valor * fator) / fator;
    }

    /** 0,4567 vira 45,7 (por cento, uma casa). */
    static double percentual(double fracao) {
        return arredondar(fracao * 100.0, 1);
    }
}
