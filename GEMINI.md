# Regras do Projeto — API Salão de Beleza

## Identidade do Projeto

- **Nome:** API Salão de Beleza
- **Tipo:** Monólito Modular — Gradle multi-módulo, Spring Boot 3.3.4, Java 21
- **Banco:** PostgreSQL com Flyway
- **Auth:** Keycloak (OAuth2 + JWT) + RBAC por roles
- **Compliance:** LGPD (Lei nº 13.709/2018) — Privacy by Design

---

## Módulos e Responsabilidades

| Módulo | Responsabilidade |
|---|---|
| `salao-common` | Utilitários transversais: `@PII`, `@Auditavel`, `UserContext` |
| `salao-security` | RBAC, JWT converter, `ApiTokenAuthenticationFilter`, auditoria AOP |
| `salao-cliente` | Domínio de clientes, consentimento, anonimização, portabilidade |
| `salao-agendamento` | Agendamentos, profissionais, serviços, catálogo |
| `salao-financeiro` | Pagamentos, comissões |
| `salao-api` | Controllers, Flyway migrations, main class, OpenAPI |

---

## Regras de Arquitetura

### Dependências entre módulos
- **Nunca** adicione dependências circulares entre módulos.
- Utilitários usados por mais de um módulo **devem** ir para `salao-common`.
- `salao-security` **não deve** depender de `salao-cliente` nem `salao-agendamento`.
- Controllers ficam **exclusivamente** em `salao-api`.

### Camada de apresentação
- Controllers **nunca** retornam entidades JPA diretamente — sempre use DTOs (`*ResponseDTO`).
- Cada domínio deve ter `*ResponseDTO`, `*UpdateDTO` e `*CreateDTO` separados.
- Validações de negócio ficam nos Services, nunca nos Controllers.

### Banco de Dados
- Toda alteração de schema é feita via **migration Flyway** (`V{n}__descricao.sql`).
- **Nunca** use `ddl-auto: create` ou `ddl-auto: update` — apenas `validate`.
- Migrations são **imutáveis** após commit — crie uma nova migration para corrigir.
- Use `ON CONFLICT DO NOTHING` para dados de seed (idempotência).

---

## Regras de Segurança (OBRIGATÓRIAS)

### Autenticação e Autorização
- Todo endpoint **deve** ter `@PreAuthorize` com a role mínima necessária.
- Use `hasRole('RECEPTION')`, `hasRole('CUSTOMER')`, `hasRole('PROFESSIONAL')`.
- O isolamento de dados para `ROLE_CUSTOMER` é feito via `UserContext` + `keycloakUserId`, **nunca** via parâmetro de URL controlado pelo usuário.
- Rotas de relatório e financeiro são **sempre** restritas a `ROLE_RECEPTION`.

### Dados Pessoais (LGPD)
- Todo campo de PII **deve** ser anotado com `@PII` de `salao-common`.
- Nunca logue dados pessoais em plain-text (nome, email, telefone, keycloakUserId).
- `show-sql` deve permanecer `false` em todas as configurações.
- Toda operação sobre PII que dispara `@Auditavel` deve especificar `acao` e `entidade`.

### API Token
- O valor do token **nunca** deve ser hardcoded — use `${API_TOKEN_SECRET}`.
- Em ambientes de produção, o token padrão `salao-secret-api-token-123` **não pode** ser usado.

---

## Regras de Código

### Java
- Use **Java 21** — aproveite records, sealed classes, pattern matching e text blocks onde aplicável.
- Use **Lombok** para reduzir boilerplate: `@Value`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`.
- Services são anotados com `@Service` + `@RequiredArgsConstructor` (injeção via construtor).
- Métodos de leitura em services devem ter `@Transactional(readOnly = true)`.
- Use `Optional` em vez de retornar `null` em repositories.

### Nomenclatura
- Entidades: `PascalCase` sem sufixo (ex: `Cliente`, `Agendamento`).
- DTOs: `{Entidade}{Propósito}DTO` (ex: `ClienteResponseDTO`, `ClienteUpdateDTO`).
- Repositories: `{Entidade}Repository`.
- Services: `{Entidade}Service`.
- Controllers: `{Entidade}Controller` em `salao-api`.

### Testes
- Ao criar um novo Service ou endpoint, crie o teste unitário correspondente.
- Use `@MockBean` para dependências externas em testes de Controller.
- Dados de teste nunca usam IDs sequenciais (`1`, `2`) — use UUIDs fixos.

---

## Regras de Migrations

- Nomeie migrations como: `V{n}__{verbo}_{substantivo}.sql` (ex: `V7__add_campos_financeiro.sql`).
- Inclua comentário de cabeçalho explicando o propósito da migration.
- Use `IF NOT EXISTS` / `IF EXISTS` para tornar migrations reentrantes onde possível.
- Ao adicionar coluna com `NOT NULL`, sempre forneça `DEFAULT` ou `UPDATE` para registros existentes.

---

## Configuração de Ambiente

| Variável | Descrição | Padrão |
|---|---|---|
| `DB_URL` | URL JDBC do banco | `jdbc:postgresql://localhost:5432/salao_db` |
| `DB_USER` | Usuário do banco | `admin` |
| `DB_PASSWORD` | Senha do banco | `admin` |
| `API_TOKEN_SECRET` | Token do header `x-api-token` | `salao-secret-api-token-123` (dev only) |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Issuer Keycloak | `http://192.168.18.200:8080/realms/salao-realm` |
