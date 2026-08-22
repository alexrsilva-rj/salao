package com.salao.api.controller;

import com.salao.agendamento.query.AgendamentoQueryService;
import com.salao.agendamento.query.dto.RelatorioAgendamentoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Buscas e relatórios gerenciais")
@SecurityRequirement(name = "keycloakOAuth")
public class RelatorioAgendamentoController {

   private final AgendamentoQueryService agendamentoQueryService;

   @GetMapping
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Buscar relatório de agendamentos por período")
   public ResponseEntity<List<RelatorioAgendamentoDTO>> buscarPorPeriodo(
           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
       
       List<RelatorioAgendamentoDTO> relatorio = agendamentoQueryService.buscarRelatorioPorPeriodo(inicio, fim);
       return ResponseEntity.ok(relatorio);
   }
}
