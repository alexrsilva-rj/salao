-- ─────────────────────────────────────────────────────────────────────────────
-- V4 — LGPD: Campos de privacidade na tabela cliente
-- ─────────────────────────────────────────────────────────────────────────────

-- Vínculo com identidade Keycloak (isolamento de dados por titular)
ALTER TABLE cliente
    ADD COLUMN keycloak_user_id VARCHAR(255) UNIQUE;

-- Consentimento explícito (Art. 8º LGPD)
ALTER TABLE cliente
    ADD COLUMN consentimento_termos_aceito   BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN data_consentimento_termos     TIMESTAMP,
    ADD COLUMN consentimento_notificacoes    BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN data_consentimento_notificacoes TIMESTAMP,
    ADD COLUMN consentimento_marketing       BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN data_consentimento_marketing  TIMESTAMP;

-- Anonimização / Direito ao Esquecimento (Art. 18, VI LGPD)
ALTER TABLE cliente
    ADD COLUMN anonimizado       BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN data_anonimizacao TIMESTAMP;

-- Controle de inatividade para expurgo automático (minimização contínua)
ALTER TABLE cliente
    ADD COLUMN ultima_atividade TIMESTAMP;

-- Índice para o job de expurgo (busca por inatividade)
CREATE INDEX idx_cliente_ultima_atividade ON cliente (ultima_atividade)
    WHERE anonimizado = FALSE;

-- Índice para lookups por keycloak_user_id
CREATE INDEX idx_cliente_keycloak_user_id ON cliente (keycloak_user_id);

-- Inicializa ultima_atividade com data_criacao para registros existentes
UPDATE cliente SET ultima_atividade = data_criacao WHERE ultima_atividade IS NULL;
