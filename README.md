# API Salão de Beleza

> Sistema de gestão para salão de beleza com agendamento, cadastro de clientes, controle financeiro e emissão de relatórios.

---

## Sumário

1. [Visão Geral](#visão-geral)
2. [Stack de Tecnologias](#stack-de-tecnologias)
3. [Arquitetura e Módulos](#arquitetura-e-módulos)
4. [Pré-requisitos](#pré-requisitos)
5. [Ambiente de Desenvolvimento (Dev)](#ambiente-de-desenvolvimento-dev)
6. [Como executar](#como-executar)
7. [Endpoints da API](#endpoints-da-api)
8. [Segurança e Autenticação](#segurança-e-autenticação)
9. [Banco de Dados](#banco-de-dados)
10. [Configuração da Aplicação](#configuração-da-aplicação)
11. [Deploy e Infraestrutura](#deploy-e-infraestrutura)
12. [CI/CD (GitLab)](#cicd-gitlab)
13. [Coleção Postman](#coleção-postman)
14. [Regras de Negócio](#regras-de-negócio)

---

## Visão Geral

| Atributo        | Valor                   |
|-----------------|-------------------------|
| **Framework**   | Spring Boot `3.3.4`     |
| **Java**        | Java 21                 |
| **Build**       | Gradle (multi-módulo)   |
| **Arquitetura** | Monólito Modular        |
| **Banco**       | PostgreSQL              |
| **Auth**        | Keycloak (OAuth2 / JWT) |
| **Versão**      | `1.0.0`                 |

A aplicação é um **monólito modular** — empacotada em um único JAR executável (`salao-api-1.0.0.jar`), organizada internamente em módulos Gradle com responsabilidades de domínio bem delimitadas.

---

## Stack de Tecnologias

- **Spring Boot 3.3.4** — Web, Data JPA, Security, Validation, OAuth2 Resource Server
- **Java 21**
- **PostgreSQL** — banco de dados relacional
- **Flyway** — migrações de schema (`flyway-core` + `flyway-database-postgresql`)
- **Keycloak** — provedor OAuth2 / OpenID Connect
- **SpringDoc OpenAPI 2.5.0** — Swagger UI em `/swagger-ui/**`
- **Lombok** — redução de boilerplate
- **Docker** + **Kubernetes** — containerização e orquestração
- **GitLab CI/CD** — pipeline de build, package e deploy

---

## Arquitetura e Módulos

O projeto é um **Gradle multi-módulo** com a seguinte estrutura:

```
salao-root
├── salao-common         # Utilitários compartilhados (sem código ainda)
├── salao-security       # Configuração Spring Security + filtro API Token
├── salao-cliente        # Domínio: cadastro de clientes
├── salao-agendamento    # Domínio: agendamentos, profissionais, serviços e relatórios
├── salao-financeiro     # Domínio: pagamentos e comissões
└── salao-api            # Ponto de entrada: controllers, OpenAPI, main class, Flyway
```

**Grafo de dependências:**

```
salao-api
  ├── salao-common
  ├── salao-security
  ├── salao-cliente        → salao-common
  ├── salao-agendamento    → salao-common, salao-cliente
  └── salao-financeiro     → salao-common, salao-agendamento
```

---

## Pré-requisitos

- Java 21+
- Gradle (ou use o Wrapper `./gradlew`)
- PostgreSQL rodando em `localhost:5432` com banco `salao_db`
- Keycloak rodando com o realm `salao-realm` configurado

---

## Como executar

### 1. Build do JAR

```bash
./gradlew :salao-api:bootJar
```

O artefato será gerado em `salao-api/build/libs/salao-api-1.0.0.jar`.

### 2. Executar localmente

```bash
java -jar salao-api/build/libs/salao-api-1.0.0.jar
```

Ou configurando variáveis de ambiente para sobrescrever os padrões:

```bash
DB_URL=jdbc:postgresql://localhost:5432/salao_db \
DB_USER=admin \
DB_PASSWORD=admin \
java -jar salao-api/build/libs/salao-api-1.0.0.jar
```

### 3. Via Docker

```bash
# Build da imagem
docker build -t salao-api:1.0.0 .

# Executar o container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/salao_db \
  -e DB_USER=admin \
  -e DB_PASSWORD=admin \
  salao-api:1.0.0
```

A aplicação estará disponível em `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Endpoints da API

> **Base URL:** `http://localhost:8080`  
> Todos os endpoints exigem autenticação via **JWT Keycloak** ou header **`x-api-token`** com a authority `SCOPE_agendamento:escrever`.

### Clientes — `/api/clientes`

| Método | Path               | Descrição                  |
|--------|--------------------|----------------------------|
| `POST` | `/api/clientes`    | Cadastrar novo cliente (body JSON) |
| `GET`  | `/api/clientes`    | Listar todos os clientes   |
| `GET`  | `/api/clientes/{id}` | Buscar cliente por UUID  |

### Agendamentos — `/api/agendamentos`

| Método | Path                 | Descrição                          | Query Params |
|--------|----------------------|------------------------------------|---|
| `POST` | `/api/agendamentos`  | Criar novo agendamento             | `clienteId`, `profissionalId`, `servicoId`, `dataHoraInicio` |
| `GET`  | `/api/agendamentos`  | Listar todos os agendamentos       | — |

### Catálogo — `/api/catalogo`

| Método | Path                          | Descrição               |
|--------|-------------------------------|-------------------------|
| `GET`  | `/api/catalogo/profissionais` | Listar profissionais    |
| `GET`  | `/api/catalogo/servicos`      | Listar serviços         |

### Financeiro — `/api/financeiro`

| Método | Path              | Descrição                            | Query Params |
|--------|-------------------|--------------------------------------|---|
| `POST` | `/api/financeiro` | Registrar pagamento e calcular comissão | `agendamentoId`, `percentualComissao`, `formaPagamento` |

### Relatórios — `/api/relatorios/agendamentos`

| Método | Path                           | Descrição                        | Query Params |
|--------|--------------------------------|----------------------------------|---|
| `GET`  | `/api/relatorios/agendamentos` | Relatório de agendamentos por período | `inicio`, `fim` (ISO DateTime) |

### Endpoints públicos (sem autenticação)

| Path               | Descrição              |
|--------------------|------------------------|
| `/swagger-ui/**`   | Swagger UI interativo  |
| `/v3/api-docs/**`  | Especificação OpenAPI  |

---

## Segurança e Autenticação

A aplicação suporta dois mecanismos de autenticação em paralelo:

### 1. OAuth2 JWT via Keycloak (principal)

- Spring configurado como **OAuth2 Resource Server**
- Tokens JWT validados contra o JWKS do Keycloak
- Realm: `salao-realm`
- Scope necessário: `agendamento:escrever`

### 2. API Token via header `x-api-token` (alternativo)

- Filtro customizado `ApiTokenAuthenticationFilter`
- Token de desenvolvimento: `salao-secret-api-token-123`
- Em produção, a validação deve consultar cache ou banco de dados

> **Atenção:** Não use o token estático em ambientes de produção.

**Fluxo da cadeia de segurança:**

```
Requisição
    └── ApiTokenAuthenticationFilter
            ├── x-api-token válido  →  injeta auth, passa adiante
            ├── x-api-token inválido  →  HTTP 401
            └── sem header  →  passa para OAuth2 JWT
    └── OAuth2 Resource Server (JWT Keycloak)
    └── @PreAuthorize("hasAuthority('SCOPE_agendamento:escrever')")
```

---

## Banco de Dados

O schema é gerenciado pelo **Flyway** e está em `salao-api/src/main/resources/db/migration/`.

| Migração | Descrição |
|---|---|
| `V1__create_table_cliente.sql` | Tabela `cliente` |
| `V2__create_agendamento_modules.sql` | Tabelas `profissional`, `servico`, `agendamento` |
| `V3__create_financeiro_module.sql` | Tabela `financeiro` |

As migrações são executadas automaticamente ao iniciar a aplicação.

---

## Configuração da Aplicação

Arquivo: `salao-api/src/main/resources/application.yml`

| Variável de Ambiente | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/salao_db` | URL do banco PostgreSQL |
| `DB_USER` | `admin` | Usuário do banco |
| `DB_PASSWORD` | `admin` | Senha do banco |

Configurações de segurança (Keycloak):

| Parâmetro | Valor |
|---|---|
| Issuer URI | `http://192.168.18.200:8080/realms/salao-realm` |
| JWK Set URI | `http://192.168.18.200:8080/realms/salao-realm/protocol/openid-connect/certs` |

---

## Deploy e Infraestrutura

### Dockerfile

- Imagem base: `eclipse-temurin:21-jre-alpine`
- Porta exposta: `8080`

### Kubernetes (`k8s-deployment.yaml`)

| Recurso | Detalhe |
|---|---|
| Deployment `salao-api` | 1 réplica, porta 8080 |
| Service `salao-api-service` | Tipo `ClusterIP`, porta 80 → 8080 |

Variáveis injetadas no pod:
- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-service:5432/salao_db`
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`

---

## CI/CD (GitLab)

Pipeline definido em `.gitlab-ci.yml` com três estágios:

| Estágio | Ação |
|---|---|
| `build` | `./gradlew :salao-api:bootJar` |
| `package` | `docker build` + `docker push` para o registry |
| `deploy` | `kubectl set image` + `kubectl rollout status` |

---

## Coleção Postman

A pasta `postman/` contém:

- `collection.json` — coleção **"API Salão de Beleza - Modular"** com requisições para todos os endpoints, incluindo obtenção de token Keycloak (Password Grant) e exemplos com `x-api-token`
- `env.json` — ambiente local pré-configurado com variáveis `keycloak_url`, `realm`, `api_url` e `api_token`

---

## Regras de Negócio

1. **Conflito de horário:** Ao criar um agendamento, o sistema verifica se o profissional já possui um agendamento ativo (não `CANCELADO`) sobreposto ao período solicitado.
2. **Horário de fim calculado automaticamente:** `dataHoraFim = dataHoraInicio + servico.duracaoMinutos`
3. **E-mail único por cliente:** Clientes devem ter e-mails únicos no sistema.
4. **Cálculo de comissão:**
   - `valorComissao = valorTotal × (percentualComissao / 100)`
   - `valorLiquidoSalao = valorTotal − valorComissao`
5. **Status padrão:** Agendamentos criados com `PENDENTE`; registros financeiros criados com `PAGO`.
6. **CQRS-lite para relatórios:** `AgendamentoQueryService` usa `EntityManager` com JPQL diretamente para leituras otimizadas, separado dos serviços de escrita.
