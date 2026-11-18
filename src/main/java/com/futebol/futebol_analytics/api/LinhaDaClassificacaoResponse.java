package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.estatistica.LinhaDaClassificacao;

record LinhaDaClassificacaoResponse(
        int posicao,
        String time,
        int pontos,
        int jogos,
        int vitorias,
        int empates,
        int derrotas,
        int golsPro,
        int golsContra,
        int saldoDeGols,
        double aproveitamento) {

    static LinhaDaClassificacaoResponse de(int posicao, LinhaDaClassificacao linha) {
        return new LinhaDaClassificacaoResponse(
                posicao,
                linha.time().nome(),
                linha.pontos(),
                linha.jogos(),
                linha.vitorias(),
                linha.empates(),
                linha.derrotas(),
                linha.golsPro(),
                linha.golsContra(),
                linha.saldoDeGols(),
                Numeros.arredondar(linha.aproveitamento(), 1));
    }
}
