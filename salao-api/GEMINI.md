# Regras — salao-api (Camada de Entrada)

## Controllers

- Este módulo é o **único** lugar para controllers (`@RestController`).
- Nunca adicione lógica de negócio em controllers — delegue ao Service do módulo correto.
- Todo controller deve ter `@Tag` (SpringDoc) com nome e descrição.
- Todo endpoint deve ter `@Operation` com `summary` e `description`.
- Use `@SecurityRequirement(name = "keycloakOAuth")` em todos os controllers protegidos.

## Migrations Flyway

- Todas as migrations ficam em `src/main/resources/db/migration/`.
- A versão deve ser estritamente sequencial: se a última é V6, a próxima é V7.
- Nunca modifique uma migration já commitada — crie uma nova.
- Após criar uma nova migration, verifique se `ddl-auto: validate` ainda passa com `./gradlew :salao-api:bootJar`.

## Configuração

- `application.yml` é a única fonte de configuração. Não crie `application-local.yml` sem documentar no README.
- Toda nova variável de ambiente deve ser adicionada à tabela de configuração do `README.md`.
