package br.edu.sarc.user.repository;

import br.edu.sarc.user.domain.PerfilUsuario;
import br.edu.sarc.user.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByPerfilOrderByNomeAsc(PerfilUsuario perfil);

    Page<Usuario> findAll(Pageable pageable);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
