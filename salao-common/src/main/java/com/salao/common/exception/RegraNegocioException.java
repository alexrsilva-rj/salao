package com.salao.common.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio do domínio.
 * Mapeada para HTTP 400 Bad Request pelo {@link com.salao.api.exception.GlobalExceptionHandler}.
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
