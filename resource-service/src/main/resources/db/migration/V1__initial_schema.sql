-- Flyway V1: Schema inicial do resource-service
-- Cria schema sarc_resources e tabela recurso

CREATE SCHEMA IF NOT EXISTS sarc_resources;

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
