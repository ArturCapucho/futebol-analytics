package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.aplicacao.ColetarPartidas;
import com.futebol.futebol_analytics.aplicacao.ConsultarClassificacao;
import com.futebol.futebol_analytics.aplicacao.ConsultarDesempenho;
import com.futebol.futebol_analytics.aplicacao.ConsultarPartidas;
import com.futebol.futebol_analytics.aplicacao.ConsultarResumo;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.estatistica.LinhaDaClassificacao;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Só traduz HTTP para casos de uso e de volta; não há regra de negócio aqui. */
@RestController
@RequestMapping("/api/competicoes/{codigo}/temporadas/{temporada}")
class CompeticaoController {

    private final ConsultarClassificacao consultarClassificacao;
    private final ConsultarResumo consultarResumo;
    private final ConsultarPartidas consultarPartidas;
    private final ConsultarDesempenho consultarDesempenho;
    private final ColetarPartidas coletarPartidas;

    CompeticaoController(
            ConsultarClassificacao consultarClassificacao,
            ConsultarResumo consultarResumo,
            ConsultarPartidas consultarPartidas,
            ConsultarDesempenho consultarDesempenho,
            ColetarPartidas coletarPartidas) {
        this.consultarClassificacao = consultarClassificacao;
        this.consultarResumo = consultarResumo;
        this.consultarPartidas = consultarPartidas;
        this.consultarDesempenho = consultarDesempenho;
        this.coletarPartidas = coletarPartidas;
    }

    /** Campanha em casa, fora e os últimos cinco resultados de cada time. */
    @GetMapping("/desempenho")
    List<DesempenhoResponse> desempenho(
            @PathVariable("codigo") String codigo, @PathVariable("temporada") int temporada) {
        return consultarDesempenho.executar(new Competicao(codigo, temporada)).stream()
                .map(DesempenhoResponse::de)
                .toList();
    }

    @GetMapping("/classificacao")
    List<LinhaDaClassificacaoResponse> classificacao(
            @PathVariable("codigo") String codigo, @PathVariable("temporada") int temporada) {
        List<LinhaDaClassificacao> linhas = consultarClassificacao.executar(new Competicao(codigo, temporada));
        return IntStream.range(0, linhas.size())
                .mapToObj(indice -> LinhaDaClassificacaoResponse.de(indice + 1, linhas.get(indice)))
                .toList();
    }

    @GetMapping("/resumo")
    ResumoResponse resumo(
            @PathVariable("codigo") String codigo, @PathVariable("temporada") int temporada) {
        return ResumoResponse.de(consultarResumo.executar(new Competicao(codigo, temporada)));
    }

    @GetMapping("/partidas")
    List<PartidaResponse> partidas(
            @PathVariable("codigo") String codigo, @PathVariable("temporada") int temporada) {
        return consultarPartidas.executar(new Competicao(codigo, temporada)).stream()
                .map(PartidaResponse::de)
                .toList();
    }

    /** Dispara uma coleta sob demanda. Idempotente: repetir não duplica partidas. */
    @PostMapping("/coletas")
    ColetaResponse coletar(
            @PathVariable("codigo") String codigo, @PathVariable("temporada") int temporada) {
        return ColetaResponse.de(coletarPartidas.executar(new Competicao(codigo, temporada)));
    }
}
