-- SARC — Schema relacional separado por domínio de serviço
-- Cada serviço possui seu próprio schema, eliminando acoplamento direto de banco.

-- ============================================================
-- SCHEMAS
-- ============================================================
CREATE SCHEMA IF NOT EXISTS sarc_users;
CREATE SCHEMA IF NOT EXISTS sarc_resources;
CREATE SCHEMA IF NOT EXISTS sarc_allocations;
CREATE SCHEMA IF NOT EXISTS sarc_schedules;

-- ============================================================
-- SCHEMA: sarc_users  (user-service)
-- ============================================================
CREATE TABLE IF NOT EXISTS sarc_users.usuario (
    id         SERIAL PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    email      VARCHAR(200) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    perfil     VARCHAR(20)  NOT NULL,
    criado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_email  UNIQUE (email),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('PROFESSOR', 'ADMIN'))
);

-- ============================================================
-- SCHEMA: sarc_resources  (resource-service)
-- ============================================================
CREATE TABLE IF NOT EXISTS sarc_resources.recurso (
    id          SERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    tipo        VARCHAR(50)  NOT NULL,
    numero_sala VARCHAR(20),
    localizacao VARCHAR(100),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_recurso_tipo CHECK (tipo IN ('SALA', 'LABORATORIO', 'COMPUTADOR', 'PROJETOR', 'EQUIPAMENTO'))
);

-- ============================================================
-- SCHEMA: sarc_allocations  (allocation-service)
-- Armazena IDs de professor e recurso sem FK cross-schema.
-- A integridade referencial é garantida pela lógica de negócio.
-- ============================================================
CREATE TABLE IF NOT EXISTS sarc_allocations.alocacao (
    id              SERIAL PRIMARY KEY,
    professor_id    INTEGER      NOT NULL,   -- referência lógica a sarc_users.usuario
    disciplina      VARCHAR(120) NOT NULL,
    data            DATE         NOT NULL,
    horario_inicio  TIME         NOT NULL,
    horario_fim     TIME         NOT NULL,
    criado_em       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_alocacao_horario CHECK (horario_fim > horario_inicio)
);

CREATE TABLE IF NOT EXISTS sarc_allocations.alocacao_recurso (
    alocacao_id INTEGER NOT NULL,
    recurso_id  INTEGER NOT NULL,           -- referência lógica a sarc_resources.recurso
    CONSTRAINT pk_alocacao_recurso
        PRIMARY KEY (alocacao_id, recurso_id),
    CONSTRAINT fk_alocacao_recurso_alocacao
        FOREIGN KEY (alocacao_id)
        REFERENCES sarc_allocations.alocacao (id)
        ON DELETE CASCADE
);

-- Índices do allocation-service
CREATE INDEX IF NOT EXISTS idx_alocacao_professor_id
    ON sarc_allocations.alocacao (professor_id);

CREATE INDEX IF NOT EXISTS idx_alocacao_data_horario
    ON sarc_allocations.alocacao (data, horario_inicio, horario_fim);

CREATE INDEX IF NOT EXISTS idx_alocacao_recurso_recurso_id
    ON sarc_allocations.alocacao_recurso (recurso_id);

-- ============================================================
-- SCHEMA: sarc_schedules  (schedule-service — leitura pública)
-- Views somente-leitura que consolidam dados para a grade pública.
-- O schedule-service conecta com currentSchema=sarc_schedules mas
-- lê as views que fazem join nos schemas de outros serviços.
-- ============================================================

-- View pública da grade consolidada
CREATE OR REPLACE VIEW sarc_schedules.grade_publica AS
SELECT
    a.id             AS alocacao_id,
    a.professor_id,
    u.nome           AS professor_nome,
    u.perfil         AS professor_perfil,
    a.disciplina,
    a.data,
    a.horario_inicio,
    a.horario_fim,
    r.id             AS recurso_id,
    r.nome           AS recurso_nome,
    r.tipo           AS recurso_tipo,
    r.localizacao    AS recurso_localizacao
FROM sarc_allocations.alocacao a
JOIN sarc_users.usuario u
    ON u.id = a.professor_id
    AND u.perfil = 'PROFESSOR'
JOIN sarc_allocations.alocacao_recurso ar
    ON ar.alocacao_id = a.id
JOIN sarc_resources.recurso r
    ON r.id = ar.recurso_id
    AND r.ativo = TRUE;
