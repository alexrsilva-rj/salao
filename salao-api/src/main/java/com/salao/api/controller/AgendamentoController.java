package com.salao.api.controller;

import com.salao.agendamento.dto.AgendamentoResponseDTO;
import com.salao.agendamento.service.AgendamentoService;
import com.salao.common.security.UserContext;
import com.salao.security.claims.JwtClaimsExtractor;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Criação e listagem de agendamentos")
@SecurityRequirement(name = "keycloakOAuth")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    /**
     * Cria novo agendamento com validação de ownership (Issue 11 — CWE-639).
     * CUSTOMER só pode criar agendamento para si mesmo — clienteId é derivado do token.
     * RECEPTION pode criar para qualquer cliente especificando clienteId.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Criar novo agendamento")
    public ResponseEntity<AgendamentoResponseDTO> criar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam UUID profissionalId,
            @RequestParam UUID servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHoraInicio) {
        UserContext ctx = jwtClaimsExtractor.extract();
        return ResponseEntity.ok(
                AgendamentoResponseDTO.from(
                        agendamentoService.criarAgendamento(clienteId, profissionalId, servicoId, dataHoraInicio, ctx)
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTION', 'PROFESSIONAL', 'CUSTOMER')")
    @Operation(summary = "Listar agendamentos",
               description = "CUSTOMER: apenas os próprios agendamentos. RECEPTION/PROFESSIONAL: todos.")
    public ResponseEntity<List<AgendamentoResponseDTO>> listar() {
        UserContext ctx = jwtClaimsExtractor.extract();
        return ResponseEntity.ok(
                agendamentoService.listarAgendamentos(ctx).stream()
                        .map(AgendamentoResponseDTO::from)
                        .toList()
        );
    }
}
