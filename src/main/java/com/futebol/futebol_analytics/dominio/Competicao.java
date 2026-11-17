package com.futebol.futebol_analytics.dominio;

import java.util.Locale;
import java.util.Objects;

/**
 * Uma edição de um campeonato: o código do campeonato (ex.: "BSA", "PL")
 * e o ano em que a temporada começa.
 */
public record Competicao(String codigo, int temporada) {

    private static final int PRIMEIRA_TEMPORADA_VALIDA = 1850;

    public Competicao {
        Objects.requireNonNull(codigo, "código da competição é obrigatório");
        codigo = codigo.strip().toUpperCase(Locale.ROOT);
        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("código da competição não pode ser vazio");
        }
        if (temporada < PRIMEIRA_TEMPORADA_VALIDA) {
            throw new IllegalArgumentException("temporada inválida: " + temporada);
        }
    }
}
