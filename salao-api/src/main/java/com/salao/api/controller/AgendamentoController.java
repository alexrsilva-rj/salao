package com.salao.api.controller;

import com.salao.agendamento.model.Agendamento;
import com.salao.agendamento.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Gerenciamento de agendamentos do salão")
@SecurityRequirement(name = "keycloakOAuth")
public class AgendamentoController {

   private final AgendamentoService agendamentoService;

   @PostMapping
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Criar novo agendamento", description = "Valida conflitos de horário e cria um agendamento protegido por escopo.")
   public ResponseEntity<Agendamento> criar(
           @RequestParam UUID clienteId,
           @RequestParam UUID profissionalId,
           @RequestParam UUID servicoId,
           @RequestParam LocalDateTime dataHoraInicio) {
       
       Agendamento novoAgendamento = agendamentoService.criarAgendamento(clienteId, profissionalId, servicoId, dataHoraInicio);
       return ResponseEntity.ok(novoAgendamento);
   }

   @GetMapping
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Listar agendamentos", description = "Retorna todos os agendamentos cadastrados.")
   public ResponseEntity<List<Agendamento>> listar() {
       List<Agendamento> agendamentos = agendamentoService.listarAgendamentos();
       return ResponseEntity.ok(agendamentos);
   }
}
