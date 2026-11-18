package com.futebol.futebol_analytics.previsao;

/**
 * Quão bem um modelo previu partidas que ele não tinha visto.
 *
 * @param partidasAvaliadas    partidas encerradas que tinham pelo menos um jogo anterior para treinar
 * @param acertos              vezes em que o resultado mais provável foi o que aconteceu
 * @param acertosDaLinhaDeBase acertos de quem sempre aposta no mandante, para comparação
 * @param brierScore           erro médio das probabilidades: 0 é perfeito, 2 é o pior possível;
 *                             chutar 1/3 para cada resultado dá cerca de 0,667
 */
public record AvaliacaoDoModelo(
        int partidasAvaliadas, int acertos, int acertosDaLinhaDeBase, double brierScore) {

    public double taxaDeAcerto() {
        return partidasAvaliadas == 0 ? 0.0 : (double) acertos / partidasAvaliadas;
    }

    public double taxaDeAcertoDaLinhaDeBase() {
        return partidasAvaliadas == 0 ? 0.0 : (double) acertosDaLinhaDeBase / partidasAvaliadas;
    }
}
