package com.salao.financeiro.dto;

import com.salao.financeiro.model.Financeiro;
import com.salao.financeiro.model.FormaPagamentoEnum;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de saída para operações financeiras.
 *
 * <p>Desacopla a resposta HTTP da entidade JPA {@link Financeiro},
 * evitando a exposição acidental de campos internos, relacionamentos
 * lazy e estrutura do banco de dados (Issue 10 — CWE-20).</p>
 */
@Value
@Builder
public class FinanceiroResponseDTO {

    UUID id;
    UUID agendamentoId;
    UUID profissionalId;
    String profissionalNome;
    BigDecimal valorTotal;
    BigDecimal percentualComissao;
    BigDecimal valorComissao;
    BigDecimal valorLiquidoSalao;
    FormaPagamentoEnum formaPagamento;
    String status;
    LocalDateTime dataPagamento;

    public static FinanceiroResponseDTO from(Financeiro f) {
        return FinanceiroResponseDTO.builder()
                .id(f.getId())
                .agendamentoId(f.getAgendamento() != null ? f.getAgendamento().getId() : null)
                .profissionalId(f.getProfissional() != null ? f.getProfissional().getId() : null)
                .profissionalNome(f.getProfissional() != null ? f.getProfissional().getNome() : null)
                .valorTotal(f.getValorTotal())
                .percentualComissao(f.getPercentualComissao())
                .valorComissao(f.getValorComissao())
                .valorLiquidoSalao(f.getValorLiquidoSalao())
                .formaPagamento(f.getFormaPagamento())
                .status(f.getStatus())
                .dataPagamento(f.getDataPagamento())
                .build();
    }
}
