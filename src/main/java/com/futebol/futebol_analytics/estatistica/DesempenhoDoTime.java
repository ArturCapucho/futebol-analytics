package com.futebol.futebol_analytics.estatistica;

import com.futebol.futebol_analytics.dominio.Time;
import java.util.List;

/**
 * Campanha de um time separada por mando de campo, mais a sequência recente.
 *
 * @param ultimosResultados do mais antigo para o mais recente
 */
public record DesempenhoDoTime(
        Time time,
        LinhaDaClassificacao emCasa,
        LinhaDaClassificacao foraDeCasa,
        List<ResultadoDoTime> ultimosResultados) {

    public DesempenhoDoTime {
        ultimosResultados = List.copyOf(ultimosResultados);
    }
}
