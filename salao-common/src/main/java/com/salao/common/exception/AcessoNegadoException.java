package com.salao.common.exception;

/**
 * Lançada quando um usuário tenta acessar um recurso sem permissão.
 * Mapeada para HTTP 403 Forbidden pelo {@link com.salao.api.exception.GlobalExceptionHandler}.
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
