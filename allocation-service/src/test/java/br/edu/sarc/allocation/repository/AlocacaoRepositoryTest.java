package br.edu.sarc.allocation.repository;

import br.edu.sarc.allocation.domain.Alocacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração com banco H2 in-memory (#14 da revisão técnica).
 *
 * Usa @BeforeEach com JdbcTemplate para inserir dados APÓS o Hibernate
 * criar as tabelas via ddl-auto=create-drop. Isso garante a ordem correta:
 * 1. H2 INIT cria os schemas (via INIT na URL)
 * 2. Hibernate cria as tabelas (EntityManagerFactory)
 * 3. @BeforeEach insere os dados de referência
 * 4. Teste executa
 * 5. @Sql cleanup limpa os dados entre testes
 *
 * Valida queries JPQL críticas: existeConflito e findByProfessorId.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("alloctest")
class AlocacaoRepositoryTest {

    @Autowired
    private AlocacaoRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void inserirDados() {
        // Limpa dados de execuções anteriores (na mesma sessão H2 mem)
        jdbc.execute("DELETE FROM sarc_allocations.alocacao_recurso");
        jdbc.execute("DELETE FROM sarc_allocations.alocacao");
        jdbc.execute("DELETE FROM sarc_resources.recurso");
        jdbc.execute("DELETE FROM sarc_users.usuario");

        // Usuário professor (schema sarc_users)
        jdbc.execute("INSERT INTO sarc_users.usuario (id, nome, email, perfil) " +
                "VALUES (1, 'Prof Teste', 'prof@sarc.local', 'PROFESSOR')");

        // Recursos (schema sarc_resources)
        jdbc.execute("INSERT INTO sarc_resources.recurso (id, nome, tipo, localizacao, ativo) " +
                "VALUES (1, 'Lab 301', 'LABORATORIO', 'Prédio 32', TRUE)");
        jdbc.execute("INSERT INTO sarc_resources.recurso (id, nome, tipo, localizacao, ativo) " +
                "VALUES (2, 'Lab 302', 'LABORATORIO', 'Prédio 32', TRUE)");

        // Alocações do dia 2026-08-01 (schema sarc_allocations)
        jdbc.execute("INSERT INTO sarc_allocations.alocacao " +
                "(id, professor_id, disciplina, data, horario_inicio, horario_fim, criado_em) " +
                "VALUES (1, 1, 'Engenharia de Software', '2026-08-01', '08:00:00', '10:00:00', NOW())");
        jdbc.execute("INSERT INTO sarc_allocations.alocacao " +
                "(id, professor_id, disciplina, data, horario_inicio, horario_fim, criado_em) " +
                "VALUES (2, 1, 'Sistemas Operacionais', '2026-08-01', '10:00:00', '12:00:00', NOW())");

        // Recursos alocados (schema sarc_allocations)
        jdbc.execute("INSERT INTO sarc_allocations.alocacao_recurso (alocacao_id, recurso_id) VALUES (1, 1)");
        jdbc.execute("INSERT INTO sarc_allocations.alocacao_recurso (alocacao_id, recurso_id) VALUES (2, 2)");
    }

    @Test
    void deveDetectarConflitoExato() {
        LocalDate data = LocalDate.of(2026, 8, 1);

        boolean conflito = repository.existeConflito(
                data, LocalTime.of(8, 0), LocalTime.of(10, 0),
                List.of(1L), null
        );

        assertThat(conflito).isTrue();
    }

    @Test
    void deveDetectarConflitoParcialSobreposicao() {
        LocalDate data = LocalDate.of(2026, 8, 1);
        boolean conflito = repository.existeConflito(
                data, LocalTime.of(9, 0), LocalTime.of(11, 0),
                List.of(1L), null
        );

        assertThat(conflito).isTrue();
    }

    @Test
    void naoDeveDetectarConflitoEmRecursoDiferente() {
        LocalDate data = LocalDate.of(2026, 8, 1);
        boolean conflito = repository.existeConflito(
                data, LocalTime.of(8, 0), LocalTime.of(10, 0),
                List.of(2L), null
        );

        assertThat(conflito).isFalse();
    }

    @Test
    void naoDeveDetectarConflitoEmDataDiferente() {
        boolean conflito = repository.existeConflito(
                LocalDate.of(2026, 8, 2), LocalTime.of(8, 0), LocalTime.of(10, 0),
                List.of(1L), null
        );

        assertThat(conflito).isFalse();
    }

    @Test
    void deveIgnorarPropriaAlocacaoNaEdicao() {
        LocalDate data = LocalDate.of(2026, 8, 1);
        boolean conflito = repository.existeConflito(
                data, LocalTime.of(8, 0), LocalTime.of(10, 0),
                List.of(1L), 1L
        );

        assertThat(conflito).isFalse();
    }

    @Test
    void deveListarAlocacoesDoProfessorOrdenadas() {
        List<Alocacao> minhas = repository.findByProfessorIdOrderByDataAscHorarioInicioAsc(1L);

        assertThat(minhas).hasSize(2);
        assertThat(minhas.get(0).getHorarioInicio()).isEqualTo(LocalTime.of(8, 0));
        assertThat(minhas.get(1).getHorarioInicio()).isEqualTo(LocalTime.of(10, 0));
    }
}
