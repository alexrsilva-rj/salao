package com.salao.api.controller;

import com.salao.agendamento.model.Profissional;
import com.salao.agendamento.model.Servico;
import com.salao.agendamento.repository.ProfissionalRepository;
import com.salao.agendamento.repository.ServicoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catálogo (Profissionais e Serviços)", description = "Listagem de profissionais e tipos de serviços")
@SecurityRequirement(name = "keycloakOAuth")
public class ProfissionalServicoController {

   private final ProfissionalRepository profissionalRepository;
   private final ServicoRepository servicoRepository;

   @GetMapping("/profissionais")
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Listar profissionais ativos")
   public ResponseEntity<List<Profissional>> listarProfissionais() {
       return ResponseEntity.ok(profissionalRepository.findAll());
   }

   @GetMapping("/servicos")
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Listar serviços disponíveis")
   public ResponseEntity<List<Servico>> listarServicos() {
       return ResponseEntity.ok(servicoRepository.findAll());
   }
}
