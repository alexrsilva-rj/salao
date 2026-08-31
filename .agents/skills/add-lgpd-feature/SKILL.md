---
name: add-lgpd-feature
description: >-
  Use this skill when the user asks to implement a new LGPD compliance feature,
  add a new data subject right (portability, rectification, erasure, consent),
  or extend the privacy model of the salao project.
  Examples: "adicione o direito de acesso", "implemente consentimento para X",
  "adicione campo LGPD na entidade Y".
---

# Skill: Adicionar Feature LGPD

Guia passo a passo para implementar novas funcionalidades de conformidade LGPD
no projeto salão, seguindo os padrões já estabelecidos.

## Pré-leitura obrigatória

Antes de implementar, consulte:
- [Regras do projeto](../../../GEMINI.md) — seção "Regras de Segurança (LGPD)"
- [Regras do salao-cliente](../../../salao-cliente/GEMINI.md)
- [Análise de gaps LGPD](./references/lgpd_architecture.md)

## Checklist de implementação

### 1. Modelo de dados
- [ ] Identificar se há novo campo na entidade `Cliente` (ou outra entidade com PII)
- [ ] Anotar todo campo de dado pessoal com `@PII(description = "...")`
- [ ] Criar migration `V{n}__descricao.sql` em `salao-api/src/main/resources/db/migration/`
- [ ] Usar `ADD COLUMN IF NOT EXISTS` na migration
- [ ] Fornecer `DEFAULT` para colunas `NOT NULL` com registros existentes

### 2. DTO
- [ ] Criar ou atualizar `*ResponseDTO`, `*UpdateDTO` ou DTO específico
- [ ] Nunca expor `keycloakUserId` em DTOs de resposta
- [ ] Adicionar método estático `from(Entidade)` no DTO

### 3. Service (salao-cliente ou módulo relevante)
- [ ] Criar ou atualizar método no Service
- [ ] Adicionar `@Auditavel(acao = "NOME_ACAO", entidade = "NomeEntidade")`
- [ ] Transações de leitura: `@Transactional(readOnly = true)`
- [ ] Transações de escrita: `@Transactional`
- [ ] Verificar `isAnonimizado()` antes de qualquer escrita em `Cliente`

### 4. Controller (salao-api)
- [ ] Adicionar endpoint em `ClienteController` (ou controller relevante)
- [ ] Definir `@PreAuthorize` com a role mínima correta
- [ ] Adicionar `@Operation(summary, description)` com referência ao artigo LGPD
- [ ] Retornar DTO, nunca entidade JPA

### 5. Segurança
- [ ] Adicionar rota em `SecurityConfig.authorizeHttpRequests` se necessário
- [ ] Verificar isolamento: endpoint de `ROLE_CUSTOMER` filtra por `keycloakUserId`?

### 6. Verificação
- [ ] `./gradlew :salao-api:bootJar` sem erros
- [ ] Testar via Postman com `x-api-token` (RECEPTION)
- [ ] Testar com token de `ROLE_CUSTOMER` (deve ver apenas os próprios dados)
- [ ] Verificar registro em `audit_log` após operação

## Padrão de código — exemplo de método de Service

```java
@Transactional(readOnly = true)
@Auditavel(acao = "NOME_DA_ACAO", entidade = "Cliente")
public MeuResponseDTO meuMetodo(UUID id, UserContext userContext) {
    Cliente cliente = buscarPorId(id);
    if (cliente.isAnonimizado()) {
        throw new IllegalStateException("Operação não permitida para clientes anonimizados.");
    }
    // lógica aqui
    return MeuResponseDTO.from(cliente);
}
```

## Padrão de código — exemplo de endpoint de Controller

```java
@GetMapping("/{id}/meu-recurso")
@PreAuthorize("hasAnyRole('RECEPTION', 'CUSTOMER')")
@Operation(summary = "Descrição curta (Art. 18, X LGPD)",
           description = "Descrição detalhada do direito exercido.")
public ResponseEntity<MeuResponseDTO> meuEndpoint(@PathVariable UUID id) {
    return ResponseEntity.ok(clienteService.meuMetodo(id));
}
```
