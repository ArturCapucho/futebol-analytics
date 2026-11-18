package com.futebol.futebol_analytics.previsao;

import com.futebol.futebol_analytics.dominio.Partida;
import java.util.Collection;

/**
 * Aprende um {@link ModeloDePrevisao} a partir do histórico.
 *
 * <p>Treinar e prever são separados de propósito: a avaliação precisa treinar
 * várias vezes, cada uma só com o passado de uma partida, e um modelo novo
 * (regressão, rede neural, LLM) entra como outra implementação desta interface.
 */
public interface TreinadorDeModelo {

    /** Partidas não encerradas no histórico são ignoradas. */
    ModeloDePrevisao treinar(Collection<Partida> historico);
}
