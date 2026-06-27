package br.edu.sarc.resource.service;

import br.edu.sarc.resource.domain.Recurso;
import br.edu.sarc.resource.dto.RecursoRequest;
import br.edu.sarc.resource.dto.RecursoResponse;
import br.edu.sarc.resource.exception.RecursoNotFoundException;
import br.edu.sarc.resource.mapper.RecursoMapper;
import br.edu.sarc.resource.repository.RecursoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecursoService {

    private static final Logger log = LoggerFactory.getLogger(RecursoService.class);

    private final RecursoRepository recursoRepository;

    public RecursoService(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    @Transactional(readOnly = true)
    public List<RecursoResponse> listarAtivosPublicamente() {
        return recursoRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(RecursoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecursoResponse buscarAtivoPublicamente(Long id) {
        return RecursoMapper.toResponse(
                recursoRepository.findByIdAndAtivoTrue(id)
                        .orElseThrow(() -> new RecursoNotFoundException(id))
        );
    }

    @Transactional(readOnly = true)
    public Page<RecursoResponse> listarTodos(Pageable pageable) {
        return recursoRepository.findAll(pageable)
                .map(RecursoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RecursoResponse buscarPorId(Long id) {
        return RecursoMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public RecursoResponse criar(RecursoRequest request) {
        boolean ativo = request.ativo() == null || request.ativo();
        Recurso recurso = new Recurso(
                request.nome(),
                request.tipo(),
                request.numeroSala(),
                request.localizacao(),
                ativo
        );

        Recurso salvo = recursoRepository.save(recurso);
        log.info("Recurso criado: id={}, nome={}, tipo={}", salvo.getId(), salvo.getNome(), salvo.getTipo());
        return RecursoMapper.toResponse(salvo);
    }

    @Transactional
    public RecursoResponse atualizar(Long id, RecursoRequest request) {
        Recurso recurso = buscarEntidade(id);
        recurso.setNome(request.nome());
        recurso.setTipo(request.tipo());
        recurso.setNumeroSala(request.numeroSala());
        recurso.setLocalizacao(request.localizacao());

        if (Boolean.TRUE.equals(request.ativo())) {
            recurso.ativar();
        } else if (Boolean.FALSE.equals(request.ativo())) {
            recurso.desativar();
        }

        log.info("Recurso atualizado: id={}, nome={}", recurso.getId(), recurso.getNome());
        return RecursoMapper.toResponse(recurso);
    }

    @Transactional
    public RecursoResponse ativar(Long id) {
        Recurso recurso = buscarEntidade(id);
        recurso.ativar();
        log.info("Recurso ativado: id={}, nome={}", recurso.getId(), recurso.getNome());
        return RecursoMapper.toResponse(recurso);
    }

    @Transactional
    public RecursoResponse desativar(Long id) {
        Recurso recurso = buscarEntidade(id);
        recurso.desativar();
        log.warn("Recurso desativado: id={}, nome={}", recurso.getId(), recurso.getNome());
        return RecursoMapper.toResponse(recurso);
    }

    @Transactional
    public void remover(Long id) {
        if (!recursoRepository.existsById(id)) {
            throw new RecursoNotFoundException(id);
        }

        recursoRepository.deleteById(id);
        log.info("Recurso removido: id={}", id);
    }

    private Recurso buscarEntidade(Long id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException(id));
    }
}
