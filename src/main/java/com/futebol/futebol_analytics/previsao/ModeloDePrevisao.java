package com.futebol.futebol_analytics.previsao;

import com.futebol.futebol_analytics.dominio.Partida;

/** Um modelo já treinado: recebe uma partida e devolve o que espera dela. */
public interface ModeloDePrevisao {

    Previsao prever(Partida partida);
}
