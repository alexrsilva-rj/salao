package com.salao.api.controller;

import com.salao.cliente.dto.*;
import com.salao.cliente.model.Cliente;
import com.salao.cliente.service.ClienteService;
import com.salao.common.security.UserContext;
import com.salao.security.claims.JwtClaimsExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes e direitos LGPD")
@SecurityRequirement(name = "keycloakOAuth")
public class ClienteController {

    private final ClienteService clienteService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    // ── CRUD ─────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Cadastrar novo cliente",
               description = "O campo consentimentoTermosAceito deve ser true (LGPD Art. 8º).")
    public ResponseEntity<ClienteResponseDTO> criar(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(ClienteResponseDTO.from(clienteService.criarCliente(cliente)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Listar clientes",
               description = "RECEPTION: todos os clientes. CUSTOMER: apenas o próprio registro.")
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        UserContext ctx = jwtClaimsExtractor.extract();
        return ResponseEntity.ok(
                clienteService.listarClientes(ctx).stream()
                        .map(ClienteResponseDTO::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(ClienteResponseDTO.from(clienteService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Atualizar dados do cliente (Art. 18, IV LGPD — Retificação)",
               description = "Apenas nome e telefone podem ser atualizados.")
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable UUID id,
                                                         @RequestBody ClienteUpdateDTO dto) {
        return ResponseEntity.ok(ClienteResponseDTO.from(clienteService.atualizarCliente(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTION')")
    @Operation(summary = "Anonimizar cliente (Art. 18, VI LGPD — Direito ao Esquecimento)",
               description = "Os dados pessoais são sobrescritos. O registro permanece para fins fiscais/contábeis.")
    public ResponseEntity<Void> anonimizar(@PathVariable UUID id) {
        clienteService.anonimizarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // ── Consentimento (Art. 8º LGPD) ─────────────────────────────

    @GetMapping("/{id}/consentimento")
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Consultar consentimentos do cliente")
    public ResponseEntity<ConsentimentoDTO> buscarConsentimento(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscarConsentimento(id));
    }

    @PutMapping("/{id}/consentimento")
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Atualizar consentimentos do cliente",
               description = "Permite opt-in/opt-out de notificações e marketing de forma independente.")
    public ResponseEntity<ConsentimentoDTO> atualizarConsentimento(@PathVariable UUID id,
                                                                     @RequestBody ConsentimentoDTO dto) {
        return ResponseEntity.ok(clienteService.atualizarConsentimento(id, dto));
    }

    // ── Portabilidade de Dados (Art. 18, V LGPD) ─────────────────

    @GetMapping("/{id}/dados-pessoais")
    @PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
    @Operation(summary = "Exportar todos os dados pessoais do cliente (Art. 18, V LGPD — Portabilidade)",
               description = "Retorna todos os dados do titular em formato JSON exportável.")
    public ResponseEntity<DadosPessoaisDTO> exportarDadosPessoais(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.exportarDadosPessoais(id));
    }
}
