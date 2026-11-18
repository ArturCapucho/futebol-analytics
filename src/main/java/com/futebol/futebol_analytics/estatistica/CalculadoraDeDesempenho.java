package com.futebol.futebol_analytics.estatistica;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Desempenho em casa, fora e nos últimos jogos de cada time, considerando só partidas encerradas. */
public final class CalculadoraDeDesempenho {

    private static final int TAMANHO_PADRAO_DA_SEQUENCIA = 5;

    private final int tamanhoDaSequencia;

    public CalculadoraDeDesempenho() {
        this(TAMANHO_PADRAO_DA_SEQUENCIA);
    }

    public CalculadoraDeDesempenho(int tamanhoDaSequencia) {
        if (tamanhoDaSequencia < 1) {
            throw new IllegalArgumentException("a sequência precisa de pelo menos 1 jogo");
        }
        this.tamanhoDaSequencia = tamanhoDaSequencia;
    }

    /** Um item por time, em ordem alfabética. */
    public List<DesempenhoDoTime> calcular(Collection<Partida> partidas) {
        // A ordem cronológica é garantida aqui, e não pressuposta de quem chamou.
        List<Partida> encerradas = partidas.stream()
                .filter(Partida::encerrada)
                .sorted(Comparator.comparing(Partida::dataHora))
                .toList();

        Map<String, Acumulado> porTime = new TreeMap<>();
        for (Partida partida : encerradas) {
            Placar placar = partida.placar();
            porTime.computeIfAbsent(partida.mandante().nome(), nome -> new Acumulado(partida.mandante()))
                    .somarEmCasa(placar.golsMandante(), placar.golsVisitante());
            porTime.computeIfAbsent(partida.visitante().nome(), nome -> new Acumulado(partida.visitante()))
                    .somarFora(placar.golsVisitante(), placar.golsMandante());
        }
        return porTime.values().stream().map(acumulado -> acumulado.paraDesempenho(tamanhoDaSequencia)).toList();
    }

    /** Estado mutável usado só durante o cálculo; nunca sai desta classe. */
    private static final class Acumulado {

        private final Time time;
        private LinhaDaClassificacao emCasa;
        private LinhaDaClassificacao fora;
        private final List<ResultadoDoTime> resultados = new ArrayList<>();

        Acumulado(Time time) {
            this.time = time;
            this.emCasa = LinhaDaClassificacao.vazia(time);
            this.fora = LinhaDaClassificacao.vazia(time);
        }

        void somarEmCasa(int golsFeitos, int golsSofridos) {
            emCasa = emCasa.comJogo(golsFeitos, golsSofridos);
            resultados.add(ResultadoDoTime.de(golsFeitos, golsSofridos));
        }

        void somarFora(int golsFeitos, int golsSofridos) {
            fora = fora.comJogo(golsFeitos, golsSofridos);
            resultados.add(ResultadoDoTime.de(golsFeitos, golsSofridos));
        }

        DesempenhoDoTime paraDesempenho(int tamanhoDaSequencia) {
            int inicio = Math.max(0, resultados.size() - tamanhoDaSequencia);
            return new DesempenhoDoTime(time, emCasa, fora, resultados.subList(inicio, resultados.size()));
        }
    }
}
