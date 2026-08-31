# Regras — salao-cliente

## Entidade Cliente e LGPD

- Novos campos de PII na entidade `Cliente` **devem** ser anotados com `@PII`.
- Toda adição de campo requer migration Flyway correspondente em `salao-api`.
- O campo `anonimizado` deve ser verificado antes de qualquer operação de escrita.
- O método `anonimizarCliente` é **idempotente** — não lance exceção se já anonimizado.

## Consentimento

- `consentimentoTermosAceito` não pode ser revertido para `false` — é a base legal do cadastro.
- As datas de consentimento (`dataConsentimento*`) são gerenciadas automaticamente pelo Service — nunca aceite esses campos do request do cliente.
- Opt-in/opt-out de `consentimentoNotificacoes` e `consentimentoMarketing` são finalidades separadas e independentes.

## Isolamento de Dados

- `listarClientes` e qualquer busca em lista **devem** receber `UserContext` e filtrar por `keycloakUserId` quando `ROLE_CUSTOMER`.
- Nunca exponha `keycloakUserId` em DTOs de resposta.

## ExpurgoClienteInativoJob

- O critério de inatividade é `ultimaAtividade < NOW() - 2 anos`.
- `ultimaAtividade` é atualizado por `registrarAtividade(UUID clienteId)` — chamado pelo `AgendamentoService` ao criar agendamentos.
- O job não deve lançar exceções que parem o scheduler — capture e logue individualmente.
