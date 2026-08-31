# SDD — Software Design Document
## API Salão de Beleza

> Versão: 1.0 | Data: 2026-08-30 | Status: Ativo

---

## 1. Propósito

Este documento descreve a arquitetura, requisitos, regras de negócio e decisões de design da API de gestão de salão de beleza. É a fonte de verdade para desenvolvimento, onboarding e manutenção do sistema.

---

## 2. Requisitos Funcionais

### RF-01 — Gestão de Clientes

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01.1 | O sistema deve permitir cadastro de clientes com nome, e-mail e telefone | Alta |
| RF-01.2 | O e-mail deve ser único por cliente | Alta |
| RF-01.3 | O cliente deve aceitar os Termos de Uso no cadastro (`consentimentoTermosAceito: true`) | Alta |
| RF-01.4 | O cliente pode dar opt-in/opt-out independente para notificações e marketing | Alta |
| RF-01.5 | O sistema deve permitir atualização de nome e telefone (retificação LGPD) | Alta |
| RF-01.6 | O sistema deve anonimizar dados do cliente a pedido (esquecimento LGPD) | Alta |
| RF-01.7 | O sistema deve exportar todos os dados do titular (portabilidade LGPD) | Alta |
| RF-01.8 | Clientes inativos há mais de 2 anos devem ser anonimizados automaticamente | Média |

### RF-02 — Agendamentos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-02.1 | O sistema deve permitir criação de agendamentos com cliente, profissional, serviço e data/hora | Alta |
| RF-02.2 | O sistema deve detectar e rejeitar conflitos de horário para o mesmo profissional | Alta |
| RF-02.3 | O horário de fim é calculado automaticamente: `início + duração do serviço` | Alta |
| RF-02.4 | Agendamentos são criados com status `PENDENTE` | Alta |
| RF-02.5 | O sistema deve listar agendamentos; ROLE_CUSTOMER vê apenas os próprios | Alta |

### RF-03 — Catálogo

| ID | Requisito | Prioridade |
|---|---|---|
| RF-03.1 | O sistema deve listar profissionais com nome e especialidade | Alta |
| RF-03.2 | O sistema deve listar serviços com nome, preço e duração | Alta |

### RF-04 — Financeiro

| ID | Requisito | Prioridade |
|---|---|---|
| RF-04.1 | O sistema deve registrar pagamentos vinculados a agendamentos | Alta |
| RF-04.2 | O sistema deve calcular `valorComissao = valorTotal × (percentual / 100)` | Alta |
| RF-04.3 | O sistema deve calcular `valorLiquidoSalao = valorTotal − valorComissao` | Alta |
| RF-04.4 | Registros financeiros são criados com status `PAGO` | Média |

### RF-05 — Relatórios

| ID | Requisito | Prioridade |
|---|---|---|
| RF-05.1 | O sistema deve gerar relatórios de agendamentos filtrados por período | Alta |
| RF-05.2 | Relatórios são acessíveis apenas para ROLE_RECEPTION | Alta |

### RF-06 — Auditoria LGPD

| ID | Requisito | Prioridade |
|---|---|---|
| RF-06.1 | Toda operação sobre PII deve gerar registro em `audit_log` | Alta |
| RF-06.2 | O log deve conter: usuário, role, ação, entidade, entidade_id, IP, data/hora | Alta |

---

## 3. Requisitos Não-Funcionais

### RNF-01 — Segurança

| ID | Requisito |
|---|---|
| RNF-01.1 | Autenticação via OAuth2/JWT (Keycloak) ou API Token |
| RNF-01.2 | Autorização baseada em roles (RBAC): RECEPTION, PROFESSIONAL, CUSTOMER |
| RNF-01.3 | ROLE_CUSTOMER só acessa os próprios dados (isolamento por `keycloak_user_id`) |
| RNF-01.4 | API Token externalizado via variável de ambiente |
| RNF-01.5 | Dados pessoais nunca expostos em logs SQL (`show-sql: false`) |

### RNF-02 — Conformidade

| ID | Requisito |
|---|---|
| RNF-02.1 | Conformidade com LGPD (Lei nº 13.709/2018) |
| RNF-02.2 | Privacy by Design: minimização de dados, consentimento granular, trilha de auditoria |
| RNF-02.3 | Campos de PII anotados com `@PII` para rastreabilidade |

### RNF-03 — Disponibilidade e Operação

| ID | Requisito |
|---|---|
| RNF-03.1 | Schema gerenciado por Flyway — sem `ddl-auto: create/update` |
| RNF-03.2 | Aplicação containerizável via Docker |
| RNF-03.3 | Deploy via Kubernetes com variáveis injetadas |
| RNF-03.4 | CI/CD via GitLab (build → package → deploy) |

### RNF-04 — Manutenibilidade

| ID | Requisito |
|---|---|
| RNF-04.1 | Arquitetura monólito modular com bounded contexts separados por módulo Gradle |
| RNF-04.2 | Controllers nunca expõem entidades JPA — apenas DTOs |
| RNF-04.3 | Migrations são imutáveis após commit |
| RNF-04.4 | API documentada via SpringDoc OpenAPI (Swagger UI) |

---

## 4. Decisões de Design (ADRs)

### ADR-01 — Monólito Modular em vez de Microsserviços

**Decisão:** Arquitetura monolítica com bounded contexts separados por módulos Gradle.  
**Motivação:** Simplicidade operacional (um único processo, um único banco, um único deploy) com limites claros de domínio que permitem futura extração para microsserviços se necessário.

### ADR-02 — Flyway como gerenciador de schema

**Decisão:** Flyway com `ddl-auto: validate`.  
**Motivação:** Controle total sobre o schema em produção; evita alterações acidentais em tabelas com dados reais.

### ADR-03 — Anonimização em vez de deleção física

**Decisão:** `DELETE /api/clientes/{id}` anonimiza os dados, não remove o registro.  
**Motivação:** Preservar integridade referencial com registros de agendamento e financeiro, que têm obrigação legal de retenção (legislação fiscal).

### ADR-04 — `@Auditavel` via AOP

**Decisão:** Trilha de auditoria implementada como aspecto (`AuditAspect`) que intercepta anotações `@Auditavel`.  
**Motivação:** Separação de concerns — a lógica de auditoria não polui o código de negócio. Falhas no log não propagam exceção para o fluxo principal.

### ADR-05 — `UserContext` como abstração de identidade

**Decisão:** `JwtClaimsExtractor` extrai o contexto do usuário em um `UserContext` imutável passado via Services.  
**Motivação:** Desacopla o código de negócio do mecanismo de autenticação específico (JWT vs. API Token). O isolamento de dados (`ROLE_CUSTOMER`) fica centralizado nos Services.

---

## 5. Modelo de Dados

```
cliente (15 cols)
  ├── id, nome*, email*, telefone*, keycloak_user_id*
  ├── consentimento_termos_aceito, data_consentimento_termos
  ├── consentimento_notificacoes, data_consentimento_notificacoes
  ├── consentimento_marketing, data_consentimento_marketing
  ├── anonimizado, data_anonimizacao
  └── data_criacao, ultima_atividade

profissional     → id, nome, especialidade, ativo, data_criacao
servico          → id, nome, descricao, preco, duracao_minutos, ativo
agendamento      → id, cliente_id, profissional_id, servico_id, data_hora_inicio, data_hora_fim, status, data_criacao
financeiro       → id, agendamento_id, profissional_id, valor_total, percentual_comissao, valor_comissao, valor_liquido_salao, forma_pagamento, status, data_pagamento
audit_log        → id, usuario_id, role, acao, entidade, entidade_id, ip_origem, data_hora

* = campo PII (@PII)
```

---

## 6. Estrutura de Customizações AGY

```
salao/
├── GEMINI.md                                    ← Regras raiz do projeto
├── salao-api/GEMINI.md                          ← Regras da camada de API
├── salao-security/GEMINI.md                     ← Regras de segurança
├── salao-cliente/GEMINI.md                      ← Regras do domínio de clientes
└── .agents/
    ├── SDD.md                                   ← Este documento
    └── skills/
        ├── add-lgpd-feature/
        │   ├── SKILL.md                         ← Guia para adicionar feature LGPD
        │   └── references/
        │       └── lgpd_architecture.md         ← Mapeamento artigos LGPD → endpoints
        ├── add-migration/
        │   └── SKILL.md                         ← Guia para criar migration Flyway
        └── add-endpoint/
            └── SKILL.md                         ← Guia para adicionar endpoint REST
```
