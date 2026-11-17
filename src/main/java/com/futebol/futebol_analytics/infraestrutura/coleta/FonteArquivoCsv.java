package com.futebol.futebol_analytics.infraestrutura.coleta;

import com.futebol.futebol_analytics.aplicacao.FalhaNaColetaException;
import com.futebol.futebol_analytics.aplicacao.porta.FonteDePartidas;
import com.futebol.futebol_analytics.dominio.Competicao;
import com.futebol.futebol_analytics.dominio.Partida;
import com.futebol.futebol_analytics.dominio.Placar;
import com.futebol.futebol_analytics.dominio.StatusPartida;
import com.futebol.futebol_analytics.dominio.Time;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.Resource;

/**
 * Lê partidas de um CSV separado por ponto e vírgula, com cabeçalho:
 * {@code id;competicao;temporada;rodada;dataHora;mandante;visitante;golsMandante;golsVisitante}.
 * Gols em branco significam partida ainda não disputada.
 *
 * <p>Serve para rodar o projeto sem depender de API externa nem de token.
 */
public final class FonteArquivoCsv implements FonteDePartidas {

    private static final String SEPARADOR = ";";
    private static final int COLUNAS = 9;

    private final Resource arquivo;

    public FonteArquivoCsv(Resource arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public List<Partida> buscar(Competicao competicao) {
        List<Partida> partidas = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            leitor.readLine(); // cabeçalho
            int numeroDaLinha = 1;
            String linha;
            while ((linha = leitor.readLine()) != null) {
                numeroDaLinha++;
                if (linha.isBlank()) {
                    continue;
                }
                Partida partida = interpretar(linha, numeroDaLinha);
                if (partida.competicao().equals(competicao)) {
                    partidas.add(partida);
                }
            }
        } catch (IOException e) {
            throw new FalhaNaColetaException("Não foi possível ler " + arquivo.getDescription(), e);
        }
        return partidas;
    }

    private Partida interpretar(String linha, int numeroDaLinha) {
        String[] campos = linha.split(SEPARADOR, -1);
        if (campos.length != COLUNAS) {
            throw new FalhaNaColetaException(
                    "Linha " + numeroDaLinha + ": esperadas " + COLUNAS + " colunas, encontradas " + campos.length);
        }
        try {
            boolean temPlacar = !campos[7].isBlank() && !campos[8].isBlank();
            Placar placar = temPlacar
                    ? new Placar(Integer.parseInt(campos[7].strip()), Integer.parseInt(campos[8].strip()))
                    : null;
            return new Partida(
                    campos[0].strip(),
                    new Competicao(campos[1], Integer.parseInt(campos[2].strip())),
                    Integer.parseInt(campos[3].strip()),
                    Instant.parse(campos[4].strip()),
                    new Time(campos[5]),
                    new Time(campos[6]),
                    temPlacar ? StatusPartida.ENCERRADA : StatusPartida.AGENDADA,
                    placar);
        } catch (RuntimeException e) {
            throw new FalhaNaColetaException("Linha " + numeroDaLinha + " inválida: " + e.getMessage(), e);
        }
    }
}
