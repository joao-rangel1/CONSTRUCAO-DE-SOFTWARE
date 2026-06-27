package br.edu.sarc.resource.repository;

import br.edu.sarc.resource.domain.Recurso;
import br.edu.sarc.resource.domain.TipoRecurso;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração com banco H2 in-memory (#14 da revisão técnica).
 * Valida queries JPA do repositório de recursos.
 * Flyway é desabilitado para deixar o ddl-auto criar o schema no H2.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RecursoRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RecursoRepository repository;

    private Recurso criarRecurso(String nome, TipoRecurso tipo, boolean ativo) {
        return em.persistAndFlush(new Recurso(nome, tipo, null, "Prédio 32", ativo));
    }

    @Test
    void devePersistirERecuperarRecurso() {
        criarRecurso("Laboratório 301", TipoRecurso.LABORATORIO, true);

        List<Recurso> ativos = repository.findByAtivoTrueOrderByNomeAsc();

        assertThat(ativos).hasSize(1);
        assertThat(ativos.get(0).getNome()).isEqualTo("Laboratório 301");
    }

    @Test
    void deveListarApenasRecursosAtivos() {
        criarRecurso("Lab Ativo",   TipoRecurso.LABORATORIO, true);
        criarRecurso("Lab Inativo", TipoRecurso.LABORATORIO, false);
        criarRecurso("Sala Ativa",  TipoRecurso.SALA,        true);

        List<Recurso> ativos = repository.findByAtivoTrueOrderByNomeAsc();

        assertThat(ativos).hasSize(2);
        assertThat(ativos).extracting(Recurso::getNome)
                .containsExactly("Lab Ativo", "Sala Ativa"); // ordenados
    }

    @Test
    void deveRetornarPaginaDeRecursos() {
        for (int i = 1; i <= 6; i++) {
            criarRecurso("Sala " + i, TipoRecurso.SALA, true);
        }

        Page<Recurso> pagina = repository.findAll(PageRequest.of(0, 4));

        assertThat(pagina.getTotalElements()).isEqualTo(6);
        assertThat(pagina.getContent()).hasSize(4);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    void deveNaoRetornarRecursoInativoNaBuscaPublica() {
        Recurso inativo = criarRecurso("Inativo", TipoRecurso.SALA, false);

        Optional<Recurso> resultado = repository.findByIdAndAtivoTrue(inativo.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarRecursosPorIds() {
        Recurso r1 = criarRecurso("Recurso 1", TipoRecurso.SALA,        true);
        Recurso r2 = criarRecurso("Recurso 2", TipoRecurso.LABORATORIO, true);
        criarRecurso("Recurso 3", TipoRecurso.EQUIPAMENTO, true);

        List<Recurso> resultado = repository.findByIdIn(List.of(r1.getId(), r2.getId()));

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Recurso::getId)
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());
    }
}
