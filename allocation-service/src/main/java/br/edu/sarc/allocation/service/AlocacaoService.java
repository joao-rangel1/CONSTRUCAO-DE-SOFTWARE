package br.edu.sarc.allocation.service;

import br.edu.sarc.allocation.domain.Alocacao;
import br.edu.sarc.allocation.domain.PerfilUsuario;
import br.edu.sarc.allocation.domain.Recurso;
import br.edu.sarc.allocation.domain.Usuario;
import br.edu.sarc.allocation.dto.AlocacaoPublicResponse;
import br.edu.sarc.allocation.dto.AlocacaoRequest;
import br.edu.sarc.allocation.dto.AlocacaoResponse;
import br.edu.sarc.allocation.exception.AlocacaoNotFoundException;
import br.edu.sarc.allocation.exception.BusinessException;
import br.edu.sarc.allocation.exception.ForbiddenOperationException;
import br.edu.sarc.allocation.mapper.AlocacaoMapper;
import br.edu.sarc.allocation.repository.AlocacaoRepository;
import br.edu.sarc.allocation.repository.RecursoRepository;
import br.edu.sarc.allocation.repository.UsuarioRepository;
import br.edu.sarc.allocation.security.CurrentUser;
import br.edu.sarc.allocation.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AlocacaoService {

    private static final Logger log = LoggerFactory.getLogger(AlocacaoService.class);

    private final AlocacaoRepository alocacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecursoRepository recursoRepository;
    private final CurrentUserProvider currentUserProvider;

    public AlocacaoService(
            AlocacaoRepository alocacaoRepository,
            UsuarioRepository usuarioRepository,
            RecursoRepository recursoRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.alocacaoRepository = alocacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.recursoRepository = recursoRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<AlocacaoPublicResponse> buscarPublicamente(LocalDate data, Long professorId, String disciplina, Long recursoId) {
        String disciplinaNormalizada = normalizarFiltro(disciplina);
        List<Alocacao> alocacoes = disciplinaNormalizada == null
                ? alocacaoRepository.buscarPublicamenteSemDisciplina(data, professorId, recursoId)
                : alocacaoRepository.buscarPublicamenteComDisciplina(data, professorId, disciplinaNormalizada, recursoId);

        return alocacoes
                .stream()
                .map(AlocacaoMapper::toPublicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlocacaoPublicResponse buscarPublicamentePorId(Long id) {
        return AlocacaoMapper.toPublicResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<AlocacaoResponse> listarMinhas() {
        Usuario professor = buscarUsuarioAtual();
        return alocacaoRepository.findByProfessorIdOrderByDataAscHorarioInicioAsc(professor.getId())
                .stream()
                .map(AlocacaoMapper::toResponse)
                .toList();
    }

    @Transactional
    public AlocacaoResponse criar(AlocacaoRequest request) {
        CurrentUser currentUser = currentUserProvider.get();
        Usuario professor = resolverProfessorParaEscrita(request.professorId(), currentUser);
        Set<Recurso> recursos = validarECarregarRecursos(request.recursoIds());
        validarHorario(request.horarioInicio(), request.horarioFim());
        validarConflito(null, request.data(), request.horarioInicio(), request.horarioFim(), request.recursoIds());

        Alocacao alocacao = new Alocacao(
                professor,
                request.disciplina(),
                request.data(),
                request.horarioInicio(),
                request.horarioFim(),
                recursos
        );

        Alocacao salva = alocacaoRepository.save(alocacao);
        log.info("Alocacao criada: id={}, professor={}, data={}, recursos={}",
                salva.getId(), professor.getEmail(), request.data(), request.recursoIds());
        return AlocacaoMapper.toResponse(salva);
    }

    @Transactional
    public AlocacaoResponse atualizar(Long id, AlocacaoRequest request) {
        CurrentUser currentUser = currentUserProvider.get();
        Alocacao alocacao = buscarEntidade(id);
        validarPropriedadeOuAdmin(alocacao, currentUser, "Professor so pode atualizar a propria alocacao");

        Usuario professor = resolverProfessorParaEscrita(request.professorId(), currentUser);
        Set<Recurso> recursos = validarECarregarRecursos(request.recursoIds());
        validarHorario(request.horarioInicio(), request.horarioFim());
        validarConflito(id, request.data(), request.horarioInicio(), request.horarioFim(), request.recursoIds());

        alocacao.setProfessor(professor);
        alocacao.setDisciplina(request.disciplina());
        alocacao.setData(request.data());
        alocacao.setHorarioInicio(request.horarioInicio());
        alocacao.setHorarioFim(request.horarioFim());
        alocacao.setRecursos(recursos);

        return AlocacaoMapper.toResponse(alocacao);
    }

    @Transactional
    public void remover(Long id) {
        CurrentUser currentUser = currentUserProvider.get();
        Alocacao alocacao = buscarEntidade(id);
        validarPropriedadeOuAdmin(alocacao, currentUser, "Professor so pode remover a propria alocacao");
        alocacaoRepository.delete(alocacao);
    }

    private Usuario buscarUsuarioAtual() {
        CurrentUser currentUser = currentUserProvider.get();
        return usuarioRepository.findByEmail(currentUser.email())
                .orElseThrow(() -> new ForbiddenOperationException("Usuario autenticado nao encontrado no cadastro interno"));
    }

    private Usuario resolverProfessorParaEscrita(Long professorId, CurrentUser currentUser) {
        Usuario usuarioAtual = usuarioRepository.findByEmail(currentUser.email())
                .orElseThrow(() -> new ForbiddenOperationException("Usuario autenticado nao encontrado no cadastro interno"));

        if (currentUser.admin()) {
            Long id = professorId != null ? professorId : usuarioAtual.getId();
            Usuario professor = usuarioRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("Professor informado nao encontrado"));
            if (professor.getPerfil() != PerfilUsuario.PROFESSOR) {
                throw new BusinessException("Alocacao deve estar vinculada a um professor");
            }
            return professor;
        }

        if (!currentUser.professor()) {
            throw new ForbiddenOperationException("Apenas PROFESSOR ou ADMIN podem alterar alocacoes");
        }

        if (professorId != null && !professorId.equals(usuarioAtual.getId())) {
            throw new ForbiddenOperationException("Professor so pode criar ou editar alocacao vinculada a ele mesmo");
        }

        if (usuarioAtual.getPerfil() != PerfilUsuario.PROFESSOR) {
            throw new ForbiddenOperationException("Usuario autenticado nao e professor");
        }

        return usuarioAtual;
    }

    private Set<Recurso> validarECarregarRecursos(List<Long> recursoIds) {
        List<Long> idsUnicos = recursoIds.stream().distinct().toList();
        List<Recurso> recursos = recursoRepository.findByIdIn(idsUnicos);

        if (recursos.size() != idsUnicos.size()) {
            throw new BusinessException("Um ou mais recursos informados nao foram encontrados");
        }

        recursos.stream()
                .filter(recurso -> !recurso.isAtivo())
                .findFirst()
                .ifPresent(recurso -> {
                    log.warn("Tentativa de alocar recurso inativo: id={}, nome={}", recurso.getId(), recurso.getNome());
                    throw new BusinessException("Recurso inativo nao pode ser alocado: " + recurso.getId());
                });

        return new LinkedHashSet<>(recursos);
    }

    private void validarHorario(LocalTime inicio, LocalTime fim) {
        if (!fim.isAfter(inicio)) {
            throw new BusinessException("Horario fim deve ser posterior ao horario inicio");
        }
    }

    private void validarConflito(Long alocacaoIdIgnorada, LocalDate data, LocalTime inicio, LocalTime fim, List<Long> recursoIds) {
        if (alocacaoRepository.existeConflito(data, inicio, fim, recursoIds.stream().distinct().toList(), alocacaoIdIgnorada)) {
            log.warn("Conflito de horario detectado: data={}, inicio={}, fim={}, recursos={}", data, inicio, fim, recursoIds);
            throw new BusinessException("Ja existe alocacao para o mesmo recurso, data e horario sobreposto");
        }
    }

    private void validarPropriedadeOuAdmin(Alocacao alocacao, CurrentUser currentUser, String message) {
        if (currentUser.admin()) {
            return;
        }

        Usuario usuarioAtual = usuarioRepository.findByEmail(currentUser.email())
                .orElseThrow(() -> new ForbiddenOperationException("Usuario autenticado nao encontrado no cadastro interno"));

        if (!alocacao.getProfessor().getId().equals(usuarioAtual.getId())) {
            throw new ForbiddenOperationException(message);
        }
    }

    private Alocacao buscarEntidade(Long id) {
        return alocacaoRepository.findById(id)
                .orElseThrow(() -> new AlocacaoNotFoundException(id));
    }

    private String normalizarFiltro(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
