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

   @Transactional
   public Financeiro registrarPagamento(UUID agendamentoId, BigDecimal percentualComissao, FormaPagamentoEnum formaPagamento) {
       Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
               .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));

       BigDecimal valorTotal = agendamento.getServico().getPreco();

       BigDecimal fatorComissao = percentualComissao.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
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
