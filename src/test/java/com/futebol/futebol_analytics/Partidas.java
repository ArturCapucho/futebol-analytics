package com.futebol.futebol_analytics;

import com.futebol.futebol_analytics.aplicacao.porta.RepositorioDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import com.futebol.futebol_analytics.dominio.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Apoio aos testes: cria partidas válidas com o mínimo de ruído. */
public final class Partidas {

    public static final Competicao COMPETICAO = new Competicao("TESTE", 2026);

    private static final Instant DATA = Instant.parse("2026-03-01T19:00:00Z");
    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    private Partidas() {
    }

    public static Partida encerrada(String mandante, String visitante, int golsMandante, int golsVisitante) {
        return nova(mandante, visitante, StatusPartida.ENCERRADA, new Placar(golsMandante, golsVisitante));
    }

    public static Partida agendada(String mandante, String visitante) {
        return nova(mandante, visitante, StatusPartida.AGENDADA, null);
    }

    public static Partida cancelada(String mandante, String visitante) {
        return nova(mandante, visitante, StatusPartida.CANCELADA, null);
    }

    private static Partida nova(String mandante, String visitante, StatusPartida status, Placar placar) {
        // Cada partida criada é um dia depois da anterior: a ordem de criação é a ordem cronológica.
        int numero = SEQUENCIA.incrementAndGet();
        return new Partida("teste-" + numero, COMPETICAO, 1, DATA.plus(Duration.ofDays(numero)),
                new Time(mandante), new Time(visitante), status, placar);
    }

    /** Repositório em memória, para testar casos de uso e controllers sem banco. */
    public static final class RepositorioEmMemoria implements RepositorioDePartidas {

        private final List<Partida> partidas = new ArrayList<>();

        public RepositorioEmMemoria(Partida... iniciais) {
            partidas.addAll(List.of(iniciais));
        }

        @Override
        public void salvarTodas(Collection<Partida> novas) {
            for (Partida nova : novas) {
                partidas.removeIf(existente -> existente.idExterno().equals(nova.idExterno()));
                partidas.add(nova);
            }
        }

        @Override
        public List<Partida> buscarPor(Competicao competicao) {
            return partidas.stream().filter(partida -> partida.competicao().equals(competicao)).toList();
        }
    }
}
