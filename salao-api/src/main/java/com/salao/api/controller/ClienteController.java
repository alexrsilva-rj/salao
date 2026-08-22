package com.salao.api.controller;

import com.salao.cliente.model.Cliente;
import com.salao.cliente.service.ClienteService;
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
@Tag(name = "Clientes", description = "Gerenciamento e busca de clientes")
@SecurityRequirement(name = "keycloakOAuth")
public class ClienteController {

   private final ClienteService clienteService;

   @PostMapping
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Cadastrar novo cliente")
   public ResponseEntity<Cliente> criar(@RequestBody Cliente cliente) {
       Cliente novoCliente = clienteService.criarCliente(cliente);
       return ResponseEntity.ok(novoCliente);
   }

   @GetMapping
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Listar todos os clientes")
   public ResponseEntity<List<Cliente>> listar() {
       return ResponseEntity.ok(clienteService.listarClientes());
   }

   @GetMapping("/{id}")
   @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
   @Operation(summary = "Buscar cliente por ID")
   public ResponseEntity<Cliente> buscarPorId(@PathVariable UUID id) {
       return ResponseEntity.ok(clienteService.buscarPorId(id));
   }
}
