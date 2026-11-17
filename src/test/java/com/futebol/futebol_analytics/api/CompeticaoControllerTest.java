package com.futebol.futebol_analytics.api;

import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.futebol.futebol_analytics.Partidas.RepositorioEmMemoria;
import com.futebol.futebol_analytics.aplicacao.ColetarPartidas;
import com.futebol.futebol_analytics.aplicacao.ConsultarClassificacao;
import com.futebol.futebol_analytics.aplicacao.ConsultarPartidas;
import com.futebol.futebol_analytics.aplicacao.ConsultarResumo;
import com.futebol.futebol_analytics.aplicacao.FalhaNaColetaException;
import com.futebol.futebol_analytics.aplicacao.porta.FonteDePartidas;
import com.futebol.futebol_analytics.estatistica.CalculadoraDeClassificacao;
import com.futebol.futebol_analytics.estatistica.CalculadoraDeResumo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Controller real + casos de uso reais + repositório em memória; sem subir o contexto do Spring. */
class CompeticaoControllerTest {

    private static final String BASE = "/api/competicoes/teste/temporadas/2026";

    private final RepositorioEmMemoria repositorio = new RepositorioEmMemoria(
            encerrada("Aurora", "Serrano", 2, 0),
            encerrada("Serrano", "Aurora", 1, 1),
            agendada("Aurora", "Litoral"));

    @Test
    @DisplayName("GET /classificacao devolve a tabela ordenada, com posição")
    void devolveClassificacao() throws Exception {
        mvc(competicao -> List.of()).perform(get(BASE + "/classificacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].posicao").value(1))
                .andExpect(jsonPath("$[0].time").value("Aurora"))
                .andExpect(jsonPath("$[0].pontos").value(4))
                .andExpect(jsonPath("$[0].saldoDeGols").value(2))
                .andExpect(jsonPath("$[0].aproveitamento").value(66.7));
    }

    @Test
    @DisplayName("GET /resumo devolve os números gerais")
    void devolveResumo() throws Exception {
        mvc(competicao -> List.of()).perform(get(BASE + "/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partidasEncerradas").value(2))
                .andExpect(jsonPath("$.totalDeGols").value(4))
                .andExpect(jsonPath("$.mediaDeGolsPorPartida").value(2.0))
                .andExpect(jsonPath("$.vitoriasDoMandante").value(1))
                .andExpect(jsonPath("$.empates").value(1));
    }

    @Test
    @DisplayName("GET /partidas devolve gols nulos para partida não disputada")
    void devolvePartidas() throws Exception {
        mvc(competicao -> List.of()).perform(get(BASE + "/partidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].golsMandante").value(2))
                .andExpect(jsonPath("$[2].status").value("AGENDADA"))
                .andExpect(jsonPath("$[2].golsMandante").doesNotExist());
    }

    @Test
    @DisplayName("POST /coletas executa a coleta e informa quantas partidas vieram")
    void executaColeta() throws Exception {
        mvc(competicao -> List.of(encerrada("Litoral", "Porto Real", 1, 0))).perform(post(BASE + "/coletas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competicao").value("TESTE"))
                .andExpect(jsonPath("$.partidasColetadas").value(1));
    }

    @Test
    @DisplayName("temporada inválida vira 400 e falha da fonte vira 502")
    void traduzErros() throws Exception {
        mvc(competicao -> List.of()).perform(get("/api/competicoes/teste/temporadas/12/resumo"))
                .andExpect(status().isBadRequest());

        mvc(competicao -> {
            throw new FalhaNaColetaException("fonte fora do ar");
        }).perform(post(BASE + "/coletas")).andExpect(status().isBadGateway());
    }

    private MockMvc mvc(FonteDePartidas fonte) {
        CompeticaoController controller = new CompeticaoController(
                new ConsultarClassificacao(repositorio, new CalculadoraDeClassificacao()),
                new ConsultarResumo(repositorio, new CalculadoraDeResumo()),
                new ConsultarPartidas(repositorio),
                new ColetarPartidas(fonte, repositorio));
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }
}
