package br.edu.sarc.allocation.repository;

import br.edu.sarc.allocation.domain.Alocacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AlocacaoRepository extends JpaRepository<Alocacao, Long> {

    @EntityGraph(attributePaths = {"professor", "recursos"})
    List<Alocacao> findByProfessorIdOrderByDataAscHorarioInicioAsc(Long professorId);

    @EntityGraph(attributePaths = {"professor", "recursos"})
    @Query("""
            select distinct a from Alocacao a
            join a.professor p
            join a.recursos r
            where (:data is null or a.data = :data)
              and (:professorId is null or p.id = :professorId)
              and (:recursoId is null or r.id = :recursoId)
            order by a.data asc, a.horarioInicio asc
            """)
    List<Alocacao> buscarPublicamenteSemDisciplina(
            @Param("data") LocalDate data,
            @Param("professorId") Long professorId,
            @Param("recursoId") Long recursoId
    );

    @EntityGraph(attributePaths = {"professor", "recursos"})
    @Query("""
            select distinct a from Alocacao a
            join a.professor p
            join a.recursos r
            where (:data is null or a.data = :data)
              and (:professorId is null or p.id = :professorId)
              and lower(a.disciplina) like lower(concat('%', :disciplina, '%'))
              and (:recursoId is null or r.id = :recursoId)
            order by a.data asc, a.horarioInicio asc
            """)
    List<Alocacao> buscarPublicamenteComDisciplina(
            @Param("data") LocalDate data,
            @Param("professorId") Long professorId,
            @Param("disciplina") String disciplina,
            @Param("recursoId") Long recursoId
    );

    @Query("""
            select count(distinct a) > 0 from Alocacao a
            join a.recursos r
            where a.data = :data
              and r.id in :recursoIds
              and (:ignorarAlocacaoId is null or a.id <> :ignorarAlocacaoId)
              and :horarioInicio < a.horarioFim
              and :horarioFim > a.horarioInicio
            """)
    boolean existeConflito(
            @Param("data") LocalDate data,
            @Param("horarioInicio") LocalTime horarioInicio,
            @Param("horarioFim") LocalTime horarioFim,
            @Param("recursoIds") List<Long> recursoIds,
            @Param("ignorarAlocacaoId") Long ignorarAlocacaoId
    );
}
