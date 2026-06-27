package br.edu.sarc.user.repository;

import br.edu.sarc.user.domain.PerfilUsuario;
import br.edu.sarc.user.domain.Usuario;
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
 * Valida que as queries JPA executam corretamente contra um schema real.
 * Flyway é desabilitado para deixar o ddl-auto criar o schema no H2.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UsuarioRepository repository;

    private Usuario criarUsuario(String nome, String email, PerfilUsuario perfil) {
        return em.persistAndFlush(new Usuario(nome, email, "hash_irrelevante_para_testes", perfil));
    }

    @Test
    void devePersistirERecuperarUsuario() {
        criarUsuario("Prof Teste", "prof@sarc.local", PerfilUsuario.PROFESSOR);

        Optional<Usuario> encontrado = repository.findByEmail("prof@sarc.local");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Prof Teste");
        assertThat(encontrado.get().getPerfil()).isEqualTo(PerfilUsuario.PROFESSOR);
    }

    @Test
    void deveListarApenasUsuariosComPerfilProfessor() {
        criarUsuario("Professor A", "prof.a@sarc.local", PerfilUsuario.PROFESSOR);
        criarUsuario("Professor B", "prof.b@sarc.local", PerfilUsuario.PROFESSOR);
        criarUsuario("Admin",       "admin@sarc.local",  PerfilUsuario.ADMIN);

        List<Usuario> professores = repository.findByPerfilOrderByNomeAsc(PerfilUsuario.PROFESSOR);

        assertThat(professores).hasSize(2);
        assertThat(professores).extracting(Usuario::getNome)
                .containsExactly("Professor A", "Professor B");
    }

    @Test
    void deveRetornarPaginaDeUsuarios() {
        for (int i = 1; i <= 5; i++) {
            criarUsuario("Usuario " + i, "u" + i + "@sarc.local", PerfilUsuario.PROFESSOR);
        }

        Page<Usuario> pagina = repository.findAll(PageRequest.of(0, 3));

        assertThat(pagina.getTotalElements()).isEqualTo(5);
        assertThat(pagina.getContent()).hasSize(3);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    void deveDetectarEmailDuplicado() {
        criarUsuario("Usuario Original", "dup@sarc.local", PerfilUsuario.PROFESSOR);

        assertThat(repository.existsByEmail("dup@sarc.local")).isTrue();
        assertThat(repository.existsByEmail("outro@sarc.local")).isFalse();
    }

    @Test
    void devePermitirMesmoEmailEmOutroId() {
        Usuario u = criarUsuario("Usuario Original", "dup@sarc.local", PerfilUsuario.PROFESSOR);

        assertThat(repository.existsByEmailAndIdNot("dup@sarc.local", u.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("dup@sarc.local", 999L)).isTrue();
    }
}
