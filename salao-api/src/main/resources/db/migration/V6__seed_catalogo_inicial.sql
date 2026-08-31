-- ─────────────────────────────────────────────────────────────────────────────
-- V6 — Dados iniciais do catálogo (profissionais e serviços)
-- Executada apenas uma vez pelo Flyway na primeira inicialização do banco.
-- ─────────────────────────────────────────────────────────────────────────────

-- Profissionais
INSERT INTO profissional (id, nome, especialidade, ativo, data_criacao)
VALUES
    ('a1b2c3d4-0001-0001-0001-000000000001', 'Ana Costa',    'Corte e Coloração',   TRUE, NOW()),
    ('a1b2c3d4-0001-0001-0001-000000000002', 'Bruno Lima',   'Manicure e Pedicure', TRUE, NOW()),
    ('a1b2c3d4-0001-0001-0001-000000000003', 'Carla Souza',  'Escova e Tratamento', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- Serviços
INSERT INTO servico (id, nome, descricao, preco, duracao_minutos, ativo)
VALUES
    ('b1b2c3d4-0001-0001-0001-000000000001', 'Corte Feminino',     'Corte, lavagem e secagem',      80.00,  60,  TRUE),
    ('b1b2c3d4-0001-0001-0001-000000000002', 'Corte Masculino',    'Corte simples',                  45.00,  30,  TRUE),
    ('b1b2c3d4-0001-0001-0001-000000000003', 'Coloração',          'Coloração completa com tintura', 150.00, 120, TRUE),
    ('b1b2c3d4-0001-0001-0001-000000000004', 'Manicure',           'Esmaltação simples',              35.00,  45,  TRUE),
    ('b1b2c3d4-0001-0001-0001-000000000005', 'Escova Progressiva', 'Alisamento progressivo',        200.00, 180, TRUE)
ON CONFLICT (id) DO NOTHING;
