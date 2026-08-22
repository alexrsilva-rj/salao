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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatórios gerenciais — acesso restrito a ROLE_RECEPTION")
@SecurityRequirement(name = "keycloakOAuth")
public class RelatorioAgendamentoController {

    private final AgendamentoQueryService agendamentoQueryService;

    @GetMapping
    @PreAuthorize("hasRole('RECEPTION')")
    @Operation(summary = "Relatório de agendamentos por período",
               description = "Restrito a perfil de recepção/administração. Expõe dados de clientes.")
    public ResponseEntity<List<RelatorioAgendamentoDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(agendamentoQueryService.buscarRelatorioPorPeriodo(inicio, fim));
    }
}
