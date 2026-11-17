package com.futebol.futebol_analytics.aplicacao.porta;

import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import java.util.Collection;
import java.util.List;

/** Porta de saída: onde as partidas coletadas ficam guardadas. */
public interface RepositorioDePartidas {

    /** Insere as partidas novas e atualiza as já existentes, casando por {@code idExterno}. */
    void salvarTodas(Collection<Partida> partidas);

    /** Partidas da competição em ordem cronológica. */
    List<Partida> buscarPor(Competicao competicao);
}
