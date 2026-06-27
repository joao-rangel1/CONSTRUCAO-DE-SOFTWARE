-- SARC — Dados iniciais com schemas separados por serviço

-- ============================================================
-- sarc_users: usuários de teste
-- ============================================================
INSERT INTO sarc_users.usuario (nome, email, senha_hash, perfil)
VALUES
    ('Professor Teste',    'professor@sarc.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR'),
    ('Administrador SARC', 'admin@sarc.local',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- sarc_resources: recursos de teste
-- ============================================================
INSERT INTO sarc_resources.recurso (nome, tipo, numero_sala, localizacao, ativo)
VALUES
    ('Laboratório 301', 'LABORATORIO', '301', 'Prédio 32',    TRUE),
    ('Laboratório 302', 'LABORATORIO', '302', 'Prédio 32',    TRUE),
    ('Sala 401',        'SALA',        '401', 'Prédio 32',    TRUE),
    ('Projetor 01',     'EQUIPAMENTO', NULL,  'Almoxarifado', TRUE);

-- ============================================================
-- sarc_allocations: alocações de exemplo
-- ============================================================
WITH dados AS (
    SELECT
        u.id AS professor_id,
        CASE
            WHEN EXTRACT(ISODOW FROM CURRENT_DATE) = 5 THEN CURRENT_DATE + INTERVAL '3 days'
            WHEN EXTRACT(ISODOW FROM CURRENT_DATE) = 6 THEN CURRENT_DATE + INTERVAL '2 days'
            ELSE CURRENT_DATE + INTERVAL '1 day'
        END::DATE AS proxima_data
    FROM sarc_users.usuario u
    WHERE u.email = 'professor@sarc.local'
),
alocacoes AS (
    INSERT INTO sarc_allocations.alocacao (professor_id, disciplina, data, horario_inicio, horario_fim)
    SELECT professor_id, 'Engenharia de Software', proxima_data, TIME '08:00', TIME '10:00'
    FROM dados
    UNION ALL
    SELECT professor_id, 'Sistemas Operacionais', proxima_data, TIME '10:00', TIME '12:00'
    FROM dados
    UNION ALL
    SELECT professor_id, 'Banco de Dados', proxima_data, TIME '14:00', TIME '16:00'
    FROM dados
    RETURNING id, disciplina
)
INSERT INTO sarc_allocations.alocacao_recurso (alocacao_id, recurso_id)
SELECT a.id, r.id
FROM alocacoes a
JOIN sarc_resources.recurso r
    ON (a.disciplina = 'Engenharia de Software' AND r.nome = 'Laboratório 301')
    OR (a.disciplina = 'Sistemas Operacionais'  AND r.nome = 'Laboratório 302')
    OR (a.disciplina = 'Banco de Dados'          AND r.nome = 'Sala 401');
