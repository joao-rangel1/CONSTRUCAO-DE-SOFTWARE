-- SARC — Dados iniciais com schemas separados por serviço
-- Script idempotente: pode ser reexecutado sem duplicar dados.

-- ============================================================
-- sarc_users: usuários de teste
-- ============================================================
-- 'professor@sarc.local' e 'admin@sarc.local' têm login real via Keycloak
-- (realm-sarc.json). Os demais professores são apenas dados de domínio,
-- usados para popular filtros e a grade pública.
INSERT INTO sarc_users.usuario (nome, email, senha_hash, perfil)
VALUES
    ('Professor Teste',          'professor@sarc.local',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR'),
    ('Administrador SARC',       'admin@sarc.local',           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'ADMIN'),
    ('Ana Beatriz Souza',        'ana.souza@sarc.local',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR'),
    ('Carlos Eduardo Lima',      'carlos.lima@sarc.local',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR'),
    ('Fernanda Ribeiro Alves',   'fernanda.alves@sarc.local',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR'),
    ('Marcos Vinícius Pereira',  'marcos.pereira@sarc.local',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR'),
    ('Juliana Castro Nunes',     'juliana.nunes@sarc.local',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR'),
    ('Ricardo Tavares Melo',     'ricardo.melo@sarc.local',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh32', 'PROFESSOR')
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- sarc_resources: recursos de teste
-- ============================================================
INSERT INTO sarc_resources.recurso (nome, tipo, numero_sala, localizacao, ativo)
SELECT v.nome, v.tipo, v.numero_sala, v.localizacao, TRUE
FROM (VALUES
    ('Laboratório 301',       'LABORATORIO', '301',    'Prédio 32'),
    ('Laboratório 302',       'LABORATORIO', '302',    'Prédio 32'),
    ('Laboratório 303',       'LABORATORIO', '303',    'Prédio 32'),
    ('Laboratório de Redes',  'LABORATORIO', '210',    'Prédio 33'),
    ('Sala 401',              'SALA',        '401',    'Prédio 32'),
    ('Sala 402',              'SALA',        '402',    'Prédio 32'),
    ('Sala 403',              'SALA',        '403',    'Prédio 32'),
    ('Auditório Central',     'SALA',        'AUD-01', 'Prédio 10'),
    ('Projetor 01',           'EQUIPAMENTO', NULL,     'Almoxarifado'),
    ('Projetor Portátil 02',  'PROJETOR',    NULL,     'Almoxarifado'),
    ('Notebook Dell 01',      'COMPUTADOR',  NULL,     'Almoxarifado'),
    ('Notebook Dell 02',      'COMPUTADOR',  NULL,     'Almoxarifado')
) AS v(nome, tipo, numero_sala, localizacao)
WHERE NOT EXISTS (
    SELECT 1 FROM sarc_resources.recurso r WHERE r.nome = v.nome
);

-- ============================================================
-- sarc_allocations: grade de alocações de exemplo
-- Distribuídas de segunda a sexta da semana corrente (ou da próxima,
-- se hoje já for sábado/domingo), para popular a visualização da grade.
-- ============================================================
WITH semana AS (
    SELECT CASE
        WHEN EXTRACT(ISODOW FROM CURRENT_DATE)::int <= 5
            THEN CURRENT_DATE - (EXTRACT(ISODOW FROM CURRENT_DATE)::int - 1)
        ELSE CURRENT_DATE + (8 - EXTRACT(ISODOW FROM CURRENT_DATE)::int)
    END AS segunda
),
dia_semana(offset_dias, data) AS (
    SELECT g, (SELECT segunda FROM semana) + g
    FROM generate_series(0, 4) AS g
),
novas_alocacoes(professor_email, disciplina, offset_dias, horario_inicio, horario_fim, recurso_nome) AS (
    VALUES
        ('professor@sarc.local',      'Engenharia de Software',       0, TIME '08:00', TIME '10:00', 'Laboratório 301'),
        ('professor@sarc.local',      'Sistemas Operacionais',        0, TIME '10:00', TIME '12:00', 'Laboratório 302'),
        ('professor@sarc.local',      'Banco de Dados',                0, TIME '14:00', TIME '16:00', 'Sala 401'),
        ('professor@sarc.local',      'Programação Web',              2, TIME '16:00', TIME '18:00', 'Laboratório 302'),
        ('ana.souza@sarc.local',      'Cálculo I',                     0, TIME '08:00', TIME '10:00', 'Sala 402'),
        ('ana.souza@sarc.local',      'Cálculo II',                    2, TIME '08:00', TIME '10:00', 'Sala 402'),
        ('ana.souza@sarc.local',      'Estatística',                   4, TIME '14:00', TIME '16:00', 'Sala 401'),
        ('carlos.lima@sarc.local',    'Redes de Computadores',         1, TIME '10:00', TIME '12:00', 'Laboratório de Redes'),
        ('carlos.lima@sarc.local',    'Segurança da Informação',       3, TIME '10:00', TIME '12:00', 'Laboratório de Redes'),
        ('carlos.lima@sarc.local',    'Banco de Dados II',             0, TIME '16:00', TIME '18:00', 'Notebook Dell 01'),
        ('fernanda.alves@sarc.local', 'Estrutura de Dados',            1, TIME '14:00', TIME '16:00', 'Laboratório 303'),
        ('fernanda.alves@sarc.local', 'Algoritmos',                    4, TIME '08:00', TIME '10:00', 'Laboratório 303'),
        ('marcos.pereira@sarc.local', 'Inteligência Artificial',       2, TIME '14:00', TIME '16:00', 'Laboratório 301'),
        ('marcos.pereira@sarc.local', 'Machine Learning',              3, TIME '14:00', TIME '16:00', 'Laboratório 301'),
        ('juliana.nunes@sarc.local',  'Engenharia de Requisitos',      1, TIME '08:00', TIME '10:00', 'Sala 403'),
        ('juliana.nunes@sarc.local',  'Gestão de Projetos',            3, TIME '08:00', TIME '10:00', 'Sala 403'),
        ('ricardo.melo@sarc.local',   'Arquitetura de Computadores',   2, TIME '10:00', TIME '12:00', 'Auditório Central'),
        ('ricardo.melo@sarc.local',   'Sistemas Distribuídos',         4, TIME '10:00', TIME '12:00', 'Auditório Central')
),
resolvidas AS (
    SELECT
        u.id AS professor_id,
        na.disciplina,
        ds.data,
        na.horario_inicio,
        na.horario_fim,
        na.recurso_nome
    FROM novas_alocacoes na
    JOIN sarc_users.usuario u ON u.email = na.professor_email
    JOIN dia_semana ds ON ds.offset_dias = na.offset_dias
),
a_inserir AS (
    SELECT r.*
    FROM resolvidas r
    WHERE NOT EXISTS (
        SELECT 1
        FROM sarc_allocations.alocacao al
        WHERE al.professor_id   = r.professor_id
          AND al.disciplina     = r.disciplina
          AND al.data           = r.data
          AND al.horario_inicio = r.horario_inicio
    )
),
inseridas AS (
    INSERT INTO sarc_allocations.alocacao (professor_id, disciplina, data, horario_inicio, horario_fim)
    SELECT professor_id, disciplina, data, horario_inicio, horario_fim
    FROM a_inserir
    RETURNING id, professor_id, disciplina, data, horario_inicio
)
INSERT INTO sarc_allocations.alocacao_recurso (alocacao_id, recurso_id)
SELECT i.id, r.id
FROM inseridas i
JOIN a_inserir ai
    ON ai.professor_id   = i.professor_id
    AND ai.disciplina     = i.disciplina
    AND ai.data           = i.data
    AND ai.horario_inicio = i.horario_inicio
JOIN sarc_resources.recurso r ON r.nome = ai.recurso_nome;
