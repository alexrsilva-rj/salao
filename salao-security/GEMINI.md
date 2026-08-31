# Regras — salao-security

## SecurityConfig

- Ao adicionar uma nova rota, defina a role mínima em `SecurityConfig.authorizeHttpRequests`.
- A ordem das regras de acesso importa — regras mais específicas vêm antes das genéricas.
- Nunca use `.permitAll()` em endpoints que retornam dados de domínio.

## Auditoria

- Métodos de Service que acessam ou modificam PII **devem** ser anotados com `@Auditavel`.
- `AuditService` usa `Propagation.REQUIRES_NEW` — nunca remova isso (garante persistência do log mesmo em rollback).
- Não adicione lógica de negócio em `AuditAspect` — apenas coleta e delega ao `AuditService`.

## JWT e Roles

- O mapeamento de roles vem de `realm_access.roles` do JWT do Keycloak.
- Roles no Keycloak devem seguir o padrão lowercase sem prefixo: `reception`, `professional`, `customer`.
- O conversor em `SecurityConfig.jwtAuthenticationConverter()` adiciona o prefixo `ROLE_` automaticamente.
