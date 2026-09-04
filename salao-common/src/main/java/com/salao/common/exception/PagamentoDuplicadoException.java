package com.salao.common.exception;

/**
 * Lançada quando se tenta registrar um pagamento para um agendamento já quitado.
 * Mapeada para HTTP 409 Conflict pelo {@link com.salao.api.exception.GlobalExceptionHandler}.
 */
public class PagamentoDuplicadoException extends RuntimeException {
    public PagamentoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
