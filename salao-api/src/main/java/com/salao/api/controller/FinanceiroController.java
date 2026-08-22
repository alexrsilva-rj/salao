package com.salao.api.controller;

import com.salao.financeiro.model.Financeiro;
import com.salao.financeiro.service.FinanceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/financeiro")
@RequiredArgsConstructor
@Tag(name = "Financeiro", description = "Gestão de pagamentos e comissões")
@SecurityRequirement(name = "keycloakOAuth")
public class FinanceiroController {

   private final FinanceiroService financeiroService;

   @PostMapping
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Registrar pagamento e calcular comissão de um agendamento")
   public ResponseEntity<Financeiro> registrarPagamento(
           @RequestParam UUID agendamentoId,
           @RequestParam BigDecimal percentualComissao,
           @RequestParam String formaPagamento) {
       
       Financeiro financeiro = financeiroService.registrarPagamento(agendamentoId, percentualComissao, formaPagamento);
       return ResponseEntity.ok(financeiro);
   }
}
