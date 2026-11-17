package com.futebol.futebol_analytics.api;

import com.futebol.futebol_analytics.aplicacao.FalhaNaColetaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte exceções em respostas no formato RFC 9457 (application/problem+json). */
@RestControllerAdvice
class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail requisicaoInvalida(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(FalhaNaColetaException.class)
    ProblemDetail falhaNaColeta(FalhaNaColetaException e) {
        log.error("Falha na coleta", e);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "A fonte de dados não respondeu como esperado.");
    }
}
