-- Dados de teste para AlocacaoRepositoryTest
-- As entidades @Immutable (Usuario, Recurso) foram criadas pelo Hibernate (create-drop)
-- com os campos exatos do Java. Inserimos via SQL com schema qualificado.

-- Usuário professor (schema sarc_users) — apenas: id, nome, email, perfil
INSERT INTO sarc_users.usuario (id, nome, email, perfil)
VALUES (1, 'Prof Teste', 'prof@sarc.local', 'PROFESSOR');

-- Recursos (schema sarc_resources) — apenas: id, nome, tipo, localizacao, ativo
INSERT INTO sarc_resources.recurso (id, nome, tipo, localizacao, ativo)
VALUES (1, 'Lab 301', 'LABORATORIO', 'Prédio 32', TRUE),
       (2, 'Lab 302', 'LABORATORIO', 'Prédio 32', TRUE);

-- Alocações (schema sarc_allocations = default) — criado_em é NOT NULL
INSERT INTO alocacao (id, professor_id, disciplina, data, horario_inicio, horario_fim, criado_em)
VALUES (1, 1, 'Engenharia de Software', '2026-08-01', '08:00:00', '10:00:00', CURRENT_TIMESTAMP),
       (2, 1, 'Sistemas Operacionais',  '2026-08-01', '10:00:00', '12:00:00', CURRENT_TIMESTAMP);

-- Associação alocacao_recurso (schema sarc_allocations)
INSERT INTO alocacao_recurso (alocacao_id, recurso_id) VALUES (1, 1);
INSERT INTO alocacao_recurso (alocacao_id, recurso_id) VALUES (2, 2);
