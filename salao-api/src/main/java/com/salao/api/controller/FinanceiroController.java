package com.salao.api.controller;

import com.salao.financeiro.dto.FinanceiroResponseDTO;
import com.salao.financeiro.model.FormaPagamentoEnum;
import com.salao.financeiro.service.FinanceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/financeiro")
@RequiredArgsConstructor
@Validated
@Tag(name = "Financeiro", description = "Gestão de pagamentos e comissões")
@SecurityRequirement(name = "keycloakOAuth")
public class FinanceiroController {

   private final FinanceiroService financeiroService;

   /**
    * Registra o pagamento de um agendamento e calcula a comissão do profissional.
    *
    * <p><strong>Validações de segurança aplicadas (Issues 10 e 18):</strong>
    * <ul>
    *   <li>{@code percentualComissao}: obrigatório, entre 0.00 e 100.00 inclusive</li>
    *   <li>{@code formaPagamento}: tipado via {@link FormaPagamentoEnum} — rejeita strings arbitrárias</li>
    *   <li>Duplicidade: o service verifica se o agendamento já foi pago (HTTP 409)</li>
    *   <li>Resposta via {@link FinanceiroResponseDTO} — entidade JPA não exposta diretamente</li>
    * </ul></p>
    */
   @PostMapping
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Registrar pagamento e calcular comissão de um agendamento")
   public ResponseEntity<FinanceiroResponseDTO> registrarPagamento(
           @RequestParam @NotNull UUID agendamentoId,
           @RequestParam
           @NotNull
           @DecimalMin(value = "0.00", message = "O percentual de comissão não pode ser negativo.")
           @DecimalMax(value = "100.00", message = "O percentual de comissão não pode exceder 100%.")
           BigDecimal percentualComissao,
           @RequestParam @NotNull FormaPagamentoEnum formaPagamento) {

       return ResponseEntity.ok(
               FinanceiroResponseDTO.from(
                       financeiroService.registrarPagamento(agendamentoId, percentualComissao, formaPagamento)
               )
       );
   }
}
