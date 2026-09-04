package com.salao.financeiro.model;

/**
 * Formas de pagamento aceitas pelo salão.
 *
 * <p>Usar enum em vez de {@code String} garante que apenas valores
 * conhecidos sejam aceitos, eliminando o risco de injeção de valores
 * arbitrários no campo formaPagamento (Issue 10 — CWE-20).</p>
 */
public enum FormaPagamentoEnum {

    /** Pagamento instantâneo via PIX. */
    PIX,

    /** Dinheiro em espécie. */
    DINHEIRO,

    /** Cartão de crédito (podendo ser parcelado). */
    CARTAO_CREDITO,

    /** Cartão de débito. */
    CARTAO_DEBITO
}
