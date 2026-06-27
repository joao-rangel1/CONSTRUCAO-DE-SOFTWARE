-- Flyway V1: Schema inicial do user-service
-- Cria schema sarc_users e tabela usuario

CREATE SCHEMA IF NOT EXISTS sarc_users;

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
