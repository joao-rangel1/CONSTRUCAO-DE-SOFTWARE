package br.edu.sarc.allocation.service;

import br.edu.sarc.allocation.domain.Alocacao;
import br.edu.sarc.allocation.domain.PerfilUsuario;
import br.edu.sarc.allocation.domain.Recurso;
import br.edu.sarc.allocation.domain.TipoRecurso;
import br.edu.sarc.allocation.domain.Usuario;
import br.edu.sarc.allocation.dto.AlocacaoRequest;
import br.edu.sarc.allocation.exception.BusinessException;
import br.edu.sarc.allocation.exception.ForbiddenOperationException;
import br.edu.sarc.allocation.repository.AlocacaoRepository;
import br.edu.sarc.allocation.repository.RecursoRepository;
import br.edu.sarc.allocation.repository.UsuarioRepository;
import br.edu.sarc.allocation.security.CurrentUser;
import br.edu.sarc.allocation.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlocacaoServiceTest {

    @Mock
    private AlocacaoRepository alocacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecursoRepository recursoRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private AlocacaoService service;
    private Usuario professor;
    private Usuario outroProfessor;
    private Usuario admin;
    private Recurso laboratorio;
    private Recurso inativo;

    @BeforeEach
    void setUp() {
        service = new AlocacaoService(alocacaoRepository, usuarioRepository, recursoRepository, currentUserProvider);

        professor = usuario(1L, "Professor Teste", "professor@sarc.local", PerfilUsuario.PROFESSOR);
        outroProfessor = usuario(2L, "Outro Professor", "outro@sarc.local", PerfilUsuario.PROFESSOR);
        admin = usuario(3L, "Admin", "admin@sarc.local", PerfilUsuario.ADMIN);
        laboratorio = recurso(10L, "Laboratorio 301", TipoRecurso.LABORATORIO, true);
        inativo = recurso(11L, "Projetor Inativo", TipoRecurso.EQUIPAMENTO, false);
    }

    @Test
    void deveCriarAlocacaoValida() {
        when(currentUserProvider.get()).thenReturn(new CurrentUser("professor@sarc.local", false, true));
        when(usuarioRepository.findByEmail("professor@sarc.local")).thenReturn(Optional.of(professor));
        when(recursoRepository.findByIdIn(List.of(10L))).thenReturn(List.of(laboratorio));
        when(alocacaoRepository.existeConflito(eq(LocalDate.now().plusDays(1)), eq(LocalTime.of(8, 0)), eq(LocalTime.of(10, 0)), eq(List.of(10L)), eq(null))).thenReturn(false);
        when(alocacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.criar(request(1L, List.of(10L), LocalTime.of(8, 0), LocalTime.of(10, 0)));

        assertThat(response.professor()).isEqualTo("Professor Teste");
        assertThat(response.recursos()).hasSize(1);
    }

    @Test
    void deveBloquearConflitoDeHorario() {
        when(currentUserProvider.get()).thenReturn(new CurrentUser("professor@sarc.local", false, true));
        when(usuarioRepository.findByEmail("professor@sarc.local")).thenReturn(Optional.of(professor));
        when(recursoRepository.findByIdIn(List.of(10L))).thenReturn(List.of(laboratorio));
        when(alocacaoRepository.existeConflito(any(), any(), any(), anyList(), eq(null))).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request(1L, List.of(10L), LocalTime.of(11, 0), LocalTime.of(13, 0))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("horario sobreposto");
    }

    @Test
    void deveBloquearRecursoInativo() {
        when(currentUserProvider.get()).thenReturn(new CurrentUser("professor@sarc.local", false, true));
        when(usuarioRepository.findByEmail("professor@sarc.local")).thenReturn(Optional.of(professor));
        when(recursoRepository.findByIdIn(List.of(11L))).thenReturn(List.of(inativo));

        assertThatThrownBy(() -> service.criar(request(1L, List.of(11L), LocalTime.of(8, 0), LocalTime.of(10, 0))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Recurso inativo");
    }

    @Test
    void professorNaoDeveAlterarAlocacaoDeOutroProfessor() {
        Alocacao alocacao = alocacao(outroProfessor);
        when(currentUserProvider.get()).thenReturn(new CurrentUser("professor@sarc.local", false, true));
        when(alocacaoRepository.findById(50L)).thenReturn(Optional.of(alocacao));
        when(usuarioRepository.findByEmail("professor@sarc.local")).thenReturn(Optional.of(professor));

        assertThatThrownBy(() -> service.atualizar(50L, request(1L, List.of(10L), LocalTime.of(8, 0), LocalTime.of(10, 0))))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("propria alocacao");
    }

    @Test
    void adminDeveRemoverQualquerAlocacao() {
        Alocacao alocacao = alocacao(outroProfessor);
        when(currentUserProvider.get()).thenReturn(new CurrentUser("admin@sarc.local", true, false));
        when(alocacaoRepository.findById(50L)).thenReturn(Optional.of(alocacao));

        service.remover(50L);

        verify(alocacaoRepository).delete(alocacao);
        verify(usuarioRepository, never()).findByEmail("admin@sarc.local");
    }

    @Test
    void criacaoComProfessorIdNullDeveUsarProfessorDoToken() {
        when(currentUserProvider.get()).thenReturn(new CurrentUser("professor@sarc.local", false, true));
        when(usuarioRepository.findByEmail("professor@sarc.local")).thenReturn(Optional.of(professor));
        when(recursoRepository.findByIdIn(List.of(10L))).thenReturn(List.of(laboratorio));
        when(alocacaoRepository.existeConflito(eq(LocalDate.now().plusDays(1)), eq(LocalTime.of(8, 0)), eq(LocalTime.of(10, 0)), eq(List.of(10L)), eq(null))).thenReturn(false);
        when(alocacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.criar(request(null, List.of(10L), LocalTime.of(8, 0), LocalTime.of(10, 0)));

        assertThat(response.professor()).isEqualTo("Professor Teste");
    }

    private AlocacaoRequest request(Long professorId, List<Long> recursoIds, LocalTime inicio, LocalTime fim) {
        return new AlocacaoRequest(
                professorId,
                "Engenharia de Software",
                LocalDate.now().plusDays(1),
                inicio,
                fim,
                recursoIds
        );
    }

    private Alocacao alocacao(Usuario professor) {
        Alocacao alocacao = new Alocacao(
                professor,
                "Engenharia de Software",
                LocalDate.now().plusDays(1),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                java.util.Set.of(laboratorio)
        );
        ReflectionTestUtils.setField(alocacao, "id", 50L);
        return alocacao;
    }

    /**
     * Cria um mock de Usuario (@Immutable — sem construtor público).
     * lenient() evita UnnecessaryStubbingException quando nem todos os getters
     * são usados em cada teste individual.
     */
    private Usuario usuario(Long id, String nome, String email, PerfilUsuario perfil) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(id);
        lenient().when(usuario.getNome()).thenReturn(nome);
        lenient().when(usuario.getEmail()).thenReturn(email);
        lenient().when(usuario.getPerfil()).thenReturn(perfil);
        return usuario;
    }

    /**
     * Cria um mock de Recurso (@Immutable — sem construtor público).
     * lenient() evita UnnecessaryStubbingException quando nem todos os getters
     * são usados em cada teste individual.
     */
    private Recurso recurso(Long id, String nome, TipoRecurso tipo, boolean ativo) {
        Recurso recurso = mock(Recurso.class);
        lenient().when(recurso.getId()).thenReturn(id);
        lenient().when(recurso.getNome()).thenReturn(nome);
        lenient().when(recurso.getTipo()).thenReturn(tipo);
        lenient().when(recurso.isAtivo()).thenReturn(ativo);
        return recurso;
    }
}
