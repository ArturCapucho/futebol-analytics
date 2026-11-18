package com.futebol.futebol_analytics.previsao;

import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.Time;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Treina um {@link ModeloPoisson}: mede, para cada time, quanto ele marca e
 * sofre em casa e fora, em relação à média da liga.
 *
 * <p><b>Regularização.</b> Com poucos jogos, uma goleada isolada faria o time
 * parecer imbatível. Por isso cada time começa com {@code jogosFicticios} jogos
 * imaginários exatamente na média da liga, que vão perdendo peso conforme os
 * jogos reais se acumulam:
 * <pre>taxa = (gols reais + jogosFicticios × média) / (jogos reais + jogosFicticios)</pre>
 * Com zero jogos a força é 1.0 (média); com muitos jogos, prevalece o dado real.
 */
public final class TreinadorPoisson implements TreinadorDeModelo {

    /** Usadas quando ainda não há histórico; valores típicos de ligas de futebol. */
    static final double MEDIA_PADRAO_GOLS_MANDANTE = 1.5;
    static final double MEDIA_PADRAO_GOLS_VISITANTE = 1.1;

    private static final double JOGOS_FICTICIOS_PADRAO = 2.0;

    private final double jogosFicticios;

    public TreinadorPoisson() {
        this(JOGOS_FICTICIOS_PADRAO);
    }

    public TreinadorPoisson(double jogosFicticios) {
        if (jogosFicticios < 0) {
            throw new IllegalArgumentException("jogos fictícios não podem ser negativos");
        }
        this.jogosFicticios = jogosFicticios;
    }

    @Override
    public ModeloDePrevisao treinar(Collection<Partida> historico) {
        List<Partida> encerradas = historico.stream().filter(Partida::encerrada).toList();

        Map<Time, Acumulado> porTime = new HashMap<>();
        int golsMandantes = 0;
        int golsVisitantes = 0;
        for (Partida partida : encerradas) {
            Placar placar = partida.placar();
            golsMandantes += placar.golsMandante();
            golsVisitantes += placar.golsVisitante();
            porTime.computeIfAbsent(partida.mandante(), time -> new Acumulado())
                    .somarEmCasa(placar.golsMandante(), placar.golsVisitante());
            porTime.computeIfAbsent(partida.visitante(), time -> new Acumulado())
                    .somarFora(placar.golsVisitante(), placar.golsMandante());
        }

        double mediaMandante = mediaOuPadrao(golsMandantes, encerradas.size(), MEDIA_PADRAO_GOLS_MANDANTE);
        double mediaVisitante = mediaOuPadrao(golsVisitantes, encerradas.size(), MEDIA_PADRAO_GOLS_VISITANTE);

        Map<Time, ModeloPoisson.Forca> forcas = new HashMap<>();
        porTime.forEach((time, a) -> forcas.put(time, new ModeloPoisson.Forca(
                forcaRelativa(a.feitosEmCasa, a.jogosEmCasa, mediaMandante),
                forcaRelativa(a.sofridosEmCasa, a.jogosEmCasa, mediaVisitante),
                forcaRelativa(a.feitosFora, a.jogosFora, mediaVisitante),
                forcaRelativa(a.sofridosFora, a.jogosFora, mediaMandante))));

        return new ModeloPoisson(mediaMandante, mediaVisitante, forcas);
    }

    private double forcaRelativa(int gols, int jogos, double mediaDaLiga) {
        double taxaSuavizada = (gols + jogosFicticios * mediaDaLiga) / (jogos + jogosFicticios);
        return taxaSuavizada / mediaDaLiga;
    }

    /** Sem jogos, ou com média zero (só 0 x 0), não dá para dividir por ela: usa o valor típico. */
    private static double mediaOuPadrao(int gols, int jogos, double padrao) {
        if (jogos == 0 || gols == 0) {
            return padrao;
        }
        return (double) gols / jogos;
    }

    private static final class Acumulado {
        private int jogosEmCasa;
        private int feitosEmCasa;
        private int sofridosEmCasa;
        private int jogosFora;
        private int feitosFora;
        private int sofridosFora;

        void somarEmCasa(int feitos, int sofridos) {
            jogosEmCasa++;
            feitosEmCasa += feitos;
            sofridosEmCasa += sofridos;
        }

        void somarFora(int feitos, int sofridos) {
            jogosFora++;
            feitosFora += feitos;
            sofridosFora += sofridos;
        }
    }
}
