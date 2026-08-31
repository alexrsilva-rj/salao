---
name: add-endpoint
description: >-
  Use this skill when the user asks to add a new REST endpoint to the salao API.
  Examples: "adicione um endpoint para listar X", "crie o endpoint de criação de Y",
  "implemente o GET /api/z".
---

# Skill: Adicionar Endpoint REST

## Passos

### 1. Identificar o módulo correto

| Se o endpoint é sobre... | Service em | Controller em |
|---|---|---|
| Clientes, consentimento, PII | `salao-cliente` | `salao-api` |
| Agendamentos, profissionais, serviços | `salao-agendamento` | `salao-api` |
| Financeiro, comissões | `salao-financeiro` | `salao-api` |

### 2. Criar o DTO de resposta (se necessário)

Em `{modulo}/src/main/java/com/salao/{dominio}/dto/`:

```java
@Value
@Builder
public class {Entidade}ResponseDTO {
    // campos selecionados — nunca exponha a entidade JPA inteira
    
    public static {Entidade}ResponseDTO from({Entidade} e) {
        return {Entidade}ResponseDTO.builder()
                // mapear campos
                .build();
    }
}
```

### 3. Criar ou atualizar o Service

```java
@Transactional(readOnly = true)                          // leitura
// ou @Transactional                                     // escrita
@Auditavel(acao = "NOME_ACAO", entidade = "{Entidade}")  // se acessa PII
public {Retorno}DTO meuMetodo(UUID id, UserContext ctx) {
    // lógica de negócio
}
```

### 4. Adicionar o endpoint no Controller (salao-api)

```java
@GetMapping("/{id}/recurso")         // ou @PostMapping, @PutMapping, @DeleteMapping
@PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")   // role mínima
@Operation(summary = "...", description = "...")
public ResponseEntity<{Retorno}DTO> meuEndpoint(@PathVariable UUID id) {
    return ResponseEntity.ok(service.meuMetodo(id));
}
```

### 5. Verificar SecurityConfig

Se a rota é nova e não cai em nenhuma regra existente de `authorizeHttpRequests`, adicione-a:

```java
.requestMatchers("/api/nova-rota/**").hasRole("RECEPTION")
```

### 6. Atualizar o README

Adicionar linha na tabela de endpoints correspondente em `README.md`.

### 7. Atualizar a coleção Postman

Adicionar request em `postman/collection.json` na pasta correta.

### 8. Verificar

```bash
./gradlew :salao-api:bootJar
# Testar via Postman com x-api-token
```

## Exemplo completo

Veja [`ClienteController.java`](../../../salao-api/src/main/java/com/salao/api/controller/ClienteController.java)
como referência de controller bem estruturado com LGPD, DTOs e Swagger.
