package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.aplicacao.AvaliarModelo;
import com.futebol.futebol_analytics.aplicacao.PreverPartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Separado do {@link CompeticaoController}: previsão muda por motivos diferentes de estatística. */
@RestController
@RequestMapping("/api/competicoes/{codigo}/temporadas/{temporada}/previsoes")
class PrevisaoController {

    private final PreverPartidas preverPartidas;
    private final AvaliarModelo avaliarModelo;

    PrevisaoController(PreverPartidas preverPartidas, AvaliarModelo avaliarModelo) {
        this.preverPartidas = preverPartidas;
        this.avaliarModelo = avaliarModelo;
    }

    /** Previsões para as partidas ainda agendadas. */
    @GetMapping
    List<PrevisaoResponse> previsoes(
            @PathVariable("codigo") String codigo, @PathVariable("temporada") int temporada) {
        return preverPartidas.executar(new Competicao(codigo, temporada)).stream()
                .map(PrevisaoResponse::de)
                .toList();
    }

    /** Quanto o modelo teria acertado nas partidas já encerradas, treinando só com o passado de cada uma. */
    @GetMapping("/avaliacao")
    AvaliacaoResponse avaliacao(
            @PathVariable("codigo") String codigo, @PathVariable("temporada") int temporada) {
        return AvaliacaoResponse.de(avaliarModelo.executar(new Competicao(codigo, temporada)));
    }
}
