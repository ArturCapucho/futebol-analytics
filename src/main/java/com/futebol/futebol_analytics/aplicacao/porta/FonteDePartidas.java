package com.futebol.futebol_analytics.aplicacao.porta;

import com.futebol.futebol_analytics.aplicacao.FalhaNaColetaException;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import java.util.List;

/** Porta de saída: de onde vêm as partidas (API externa, arquivo, fila...). */
public interface FonteDePartidas {

    /**
     * @throws FalhaNaColetaException quando a fonte está indisponível ou devolve dados inválidos
     */
    List<Partida> buscar(Competicao competicao);
}
