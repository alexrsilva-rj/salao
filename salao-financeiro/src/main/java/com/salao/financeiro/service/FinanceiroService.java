package com.salao.financeiro.service;

import com.salao.agendamento.model.Agendamento;
import com.salao.agendamento.repository.AgendamentoRepository;
import com.salao.financeiro.model.Financeiro;
import com.salao.financeiro.model.FormaPagamentoEnum;
import com.salao.financeiro.repository.FinanceiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceiroService {

   private final FinanceiroRepository financeiroRepository;
   private final AgendamentoRepository agendamentoRepository;

   /**
    * Registra o pagamento de um agendamento e calcula a comissão do profissional.
    *
    * <p><strong>Validações aplicadas (Issues 10 e 18 / V3):</strong>
    * <ul>
    *   <li>Agendamento deve existir</li>
    *   <li>Agendamento não pode ter pagamento duplicado (idempotência — HTTP 409)</li>
    *   <li>Percentual de comissão já foi validado pelo controller (0.00–100.00)</li>
    *   <li>FormaPagamento tipada via enum (sem strings arbitrárias)</li>
    * </ul></p>
    *
    * @param agendamentoId      ID do agendamento a ser pago
    * @param percentualComissao percentual entre 0.00 e 100.00 (validado no controller)
    * @param formaPagamento     forma de pagamento tipada
    * @throws IllegalStateException se o agendamento já tiver pagamento registrado
    */
   @Transactional
   public Financeiro registrarPagamento(UUID agendamentoId,
                                        BigDecimal percentualComissao,
                                        FormaPagamentoEnum formaPagamento) {

       Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
               .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));

       // Issue 18 / V3: impede pagamentos duplicados para o mesmo agendamento
       if (financeiroRepository.existsByAgendamentoId(agendamentoId)) {
           throw new IllegalStateException(
                   "Este agendamento já possui um pagamento registrado. " +
                   "Não é permitido registrar pagamento duplicado.");
       }

       BigDecimal valorTotal = agendamento.getServico().getPreco();

       BigDecimal fatorComissao = percentualComissao.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
       BigDecimal valorComissao = valorTotal.multiply(fatorComissao).setScale(2, RoundingMode.HALF_UP);
       BigDecimal valorLiquidoSalao = valorTotal.subtract(valorComissao);

       Financeiro financeiro = Financeiro.builder()
               .agendamento(agendamento)
               .profissional(agendamento.getProfissional())
               .valorTotal(valorTotal)
               .percentualComissao(percentualComissao)
               .valorComissao(valorComissao)
               .valorLiquidoSalao(valorLiquidoSalao)
               .formaPagamento(formaPagamento)
               .status("PAGO")
               .build();

       return financeiroRepository.save(financeiro);
   }
}
