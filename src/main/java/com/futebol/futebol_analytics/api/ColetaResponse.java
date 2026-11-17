package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.aplicacao.ResultadoDaColeta;

record ColetaResponse(String competicao, int temporada, int partidasColetadas) {

    static ColetaResponse de(ResultadoDaColeta resultado) {
        return new ColetaResponse(
                resultado.competicao().codigo(),
                resultado.competicao().temporada(),
                resultado.partidasColetadas());
    }
}
