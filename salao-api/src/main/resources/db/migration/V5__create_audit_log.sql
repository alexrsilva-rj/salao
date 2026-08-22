-- ─────────────────────────────────────────────────────────────────────────────
-- V5 — LGPD: Tabela de trilha de auditoria para acesso a dados pessoais
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE audit_log (
    id           UUID         PRIMARY KEY,
    usuario_id   VARCHAR(255) NOT NULL,
    role         VARCHAR(100) NOT NULL,
    acao         VARCHAR(100) NOT NULL,
    entidade     VARCHAR(100) NOT NULL,
    entidade_id  VARCHAR(255),
    ip_origem    VARCHAR(45),
    data_hora    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Índices para consultas de auditoria
CREATE INDEX idx_audit_log_usuario_id ON audit_log (usuario_id);
CREATE INDEX idx_audit_log_data_hora  ON audit_log (data_hora);
CREATE INDEX idx_audit_log_acao       ON audit_log (acao);
