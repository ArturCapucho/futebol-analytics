package com.futebol.futebol_analytics.estatistica;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import java.util.Collection;

public final class CalculadoraDeResumo {

    public ResumoDaCompeticao calcular(Collection<Partida> partidas) {
        int encerradas = 0;
        int gols = 0;
        int vitoriasDoMandante = 0;
        int empates = 0;
        int vitoriasDoVisitante = 0;
        for (Partida partida : partidas) {
            if (!partida.encerrada()) {
                continue;
            }
            Placar placar = partida.placar();
            encerradas++;
            gols += placar.totalDeGols();
            switch (placar.resultado()) {
                case VITORIA_MANDANTE -> vitoriasDoMandante++;
                case EMPATE -> empates++;
                case VITORIA_VISITANTE -> vitoriasDoVisitante++;
            }
        }
        return new ResumoDaCompeticao(encerradas, gols, vitoriasDoMandante, empates, vitoriasDoVisitante);
    }
}
