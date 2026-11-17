package com.futebol.futebol_analytics.dominio;

import java.util.Objects;

/** Um clube. Dois times são o mesmo quando têm o mesmo nome. */
public record Time(String nome) {

    public Time {
        Objects.requireNonNull(nome, "nome do time é obrigatório");
        nome = nome.strip();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("nome do time não pode ser vazio");
        }
    }
}
