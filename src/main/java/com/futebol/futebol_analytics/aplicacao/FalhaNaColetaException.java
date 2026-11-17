package com.futebol.futebol_analytics.aplicacao;

/** A fonte de dados não respondeu ou respondeu algo que não dá para interpretar. */
public class FalhaNaColetaException extends RuntimeException {

    public FalhaNaColetaException(String mensagem) {
        super(mensagem);
    }

    public FalhaNaColetaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
