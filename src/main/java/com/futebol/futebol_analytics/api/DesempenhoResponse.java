package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.estatistica.DesempenhoDoTime;
import com.futebol.futebol_analytics.estatistica.LinhaDaClassificacao;
import com.futebol.futebol_analytics.estatistica.ResultadoDoTime;
import java.util.List;

/** {@code ultimosResultados} usa V, E e D, do jogo mais antigo para o mais recente. */
record DesempenhoResponse(String time, Campanha emCasa, Campanha foraDeCasa, List<String> ultimosResultados) {

    static DesempenhoResponse de(DesempenhoDoTime desempenho) {
        return new DesempenhoResponse(
                desempenho.time().nome(),
                Campanha.de(desempenho.emCasa()),
                Campanha.de(desempenho.foraDeCasa()),
                desempenho.ultimosResultados().stream().map(DesempenhoResponse::letra).toList());
    }

    private static String letra(ResultadoDoTime resultado) {
        return switch (resultado) {
            case VITORIA -> "V";
            case EMPATE -> "E";
            case DERROTA -> "D";
        };
    }

    record Campanha(int jogos, int pontos, int vitorias, int empates, int derrotas,
                    int golsPro, int golsContra, double aproveitamento) {

        static Campanha de(LinhaDaClassificacao linha) {
            return new Campanha(linha.jogos(), linha.pontos(), linha.vitorias(), linha.empates(),
                    linha.derrotas(), linha.golsPro(), linha.golsContra(),
                    Numeros.arredondar(linha.aproveitamento(), 1));
        }
    }
}
