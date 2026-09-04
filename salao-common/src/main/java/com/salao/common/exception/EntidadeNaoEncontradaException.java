package com.salao.common.exception;

/**
 * Lançada quando uma entidade não é encontrada pelo ID informado.
 * Mapeada para HTTP 404 Not Found pelo {@link com.salao.api.exception.GlobalExceptionHandler}.
 */
public class EntidadeNaoEncontradaException extends RuntimeException {
    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
