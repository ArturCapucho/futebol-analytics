package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.previsao.AvaliacaoDoModelo;

/** Taxas em percentual; Brier com três casas (quanto menor, melhor). */
record AvaliacaoResponse(
        int partidasAvaliadas,
        int acertos,
        double taxaDeAcerto,
        double taxaDeAcertoSempreMandante,
        double brierScore) {

    static AvaliacaoResponse de(AvaliacaoDoModelo avaliacao) {
        return new AvaliacaoResponse(
                avaliacao.partidasAvaliadas(),
                avaliacao.acertos(),
                Numeros.percentual(avaliacao.taxaDeAcerto()),
                Numeros.percentual(avaliacao.taxaDeAcertoDaLinhaDeBase()),
                Numeros.arredondar(avaliacao.brierScore(), 3));
    }
}
