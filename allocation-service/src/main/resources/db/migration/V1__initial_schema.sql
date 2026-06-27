-- Flyway V1: Schema inicial do allocation-service
-- Cria schema sarc_allocations e tabelas alocacao + alocacao_recurso.
-- NOTA: professor_id e recurso_id são referências lógicas (sem FK cross-schema).
--       A integridade é garantida pela lógica de negócio do AlocacaoService.

CREATE SCHEMA IF NOT EXISTS sarc_allocations;

CREATE TABLE IF NOT EXISTS sarc_allocations.alocacao (
    id             SERIAL PRIMARY KEY,
    professor_id   INTEGER      NOT NULL,
    disciplina     VARCHAR(120) NOT NULL,
    data           DATE         NOT NULL,
    horario_inicio TIME         NOT NULL,
    horario_fim    TIME         NOT NULL,
    criado_em      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_alocacao_horario CHECK (horario_fim > horario_inicio)
);

CREATE TABLE IF NOT EXISTS sarc_allocations.alocacao_recurso (
    alocacao_id INTEGER NOT NULL,
    recurso_id  INTEGER NOT NULL,
    CONSTRAINT pk_alocacao_recurso PRIMARY KEY (alocacao_id, recurso_id),
    CONSTRAINT fk_alocacao_recurso_alocacao
        FOREIGN KEY (alocacao_id)
        REFERENCES sarc_allocations.alocacao (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_alocacao_professor_id
    ON sarc_allocations.alocacao (professor_id);

CREATE INDEX IF NOT EXISTS idx_alocacao_data_horario
    ON sarc_allocations.alocacao (data, horario_inicio, horario_fim);

CREATE INDEX IF NOT EXISTS idx_alocacao_recurso_recurso_id
    ON sarc_allocations.alocacao_recurso (recurso_id);
