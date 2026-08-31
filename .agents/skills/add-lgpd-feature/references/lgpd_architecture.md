# Arquitetura LGPD — Referência

Artigos LGPD implementados e seus endpoints correspondentes:

| Art. LGPD | Direito | Endpoint |
|---|---|---|
| Art. 8º | Consentimento explícito | `POST /api/clientes` (campo `consentimentoTermosAceito: true`) |
| Art. 8º | Opt-in/opt-out | `PUT /api/clientes/{id}/consentimento` |
| Art. 18, I | Acesso aos dados | `GET /api/clientes/{id}` |
| Art. 18, IV | Retificação | `PUT /api/clientes/{id}` |
| Art. 18, V | Portabilidade | `GET /api/clientes/{id}/dados-pessoais` |
| Art. 18, VI | Esquecimento | `DELETE /api/clientes/{id}` (anonimização) |
| Art. 46 | Segurança técnica | RBAC, AuditLog, @PII, show-sql: false |

## Roles e isolamento de dados

- `ROLE_RECEPTION` — acesso a todos os dados
- `ROLE_CUSTOMER` — somente os próprios dados (filtrado por `keycloak_user_id`)
- `ROLE_PROFESSIONAL` — catálogo e agendamentos

## Campos de PII na entidade Cliente

```
nome               → @PII("Nome completo do titular")
email              → @PII("E-mail — identificador único")
telefone           → @PII("Telefone/WhatsApp")
keycloak_user_id   → @PII("Vínculo com identidade Keycloak")
```

## Fluxo de anonimização

1. Controller recebe `DELETE /api/clientes/{id}` com `ROLE_RECEPTION`
2. `ClienteService.anonimizarCliente(UUID)` — idempotente
3. Sobrescreve: `nome → "Cliente Anonimizado"`, `email → "anonimizado-{id}@removido.local"`, `telefone → null`, `keycloakUserId → null`
4. Define: `anonimizado = true`, `dataAnonimizacao = now()`
5. Histórico financeiro e de agendamentos é preservado
