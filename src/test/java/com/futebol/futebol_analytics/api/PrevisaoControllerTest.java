package com.futebol.futebol_analytics.api;

import static com.futebol.futebol_analytics.Partidas.agendada;
import static com.futebol.futebol_analytics.Partidas.encerrada;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.futebol.futebol_analytics.Partidas.RepositorioEmMemoria;
import com.futebol.futebol_analytics.aplicacao.AvaliarModelo;
import com.futebol.futebol_analytics.aplicacao.PreverPartidas;
import com.futebol.futebol_analytics.previsao.AvaliadorDeModelo;
import com.futebol.futebol_analytics.previsao.TreinadorPoisson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PrevisaoControllerTest {

    private static final String BASE = "/api/competicoes/teste/temporadas/2026/previsoes";

    private final RepositorioEmMemoria repositorio = new RepositorioEmMemoria(
            encerrada("Aurora", "Serrano", 3, 0),
            encerrada("Serrano", "Aurora", 0, 2),
            encerrada("Aurora", "Serrano", 2, 1),
            agendada("Aurora", "Serrano"));

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PrevisaoController(
                    new PreverPartidas(repositorio, new TreinadorPoisson()),
                    new AvaliarModelo(repositorio, new AvaliadorDeModelo(new TreinadorPoisson()))))
            .setControllerAdvice(new TratadorDeErros())
            .build();

    @Test
    @DisplayName("GET /previsoes prevê só as partidas agendadas, com chances em percentual")
    void devolvePrevisoes() throws Exception {
        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].mandante").value("Aurora"))
                .andExpect(jsonPath("$[0].resultadoMaisProvavel").value("VITORIA_MANDANTE"))
                .andExpect(jsonPath("$[0].placarMaisProvavel").isString());
    }

    @Test
    @DisplayName("GET /previsoes/avaliacao devolve as métricas do backtest")
    void devolveAvaliacao() throws Exception {
        mvc.perform(get(BASE + "/avaliacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partidasAvaliadas").value(2))
                .andExpect(jsonPath("$.brierScore").isNumber())
                .andExpect(jsonPath("$.taxaDeAcertoSempreMandante").value(50.0));
    }
}
