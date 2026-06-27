package br.edu.sarc.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Projeção somente-leitura da alocação do schema sarc_allocations.
 * O schedule-service apenas lê alocações para compor a grade pública.
 */
@Entity
@Immutable
@Table(name = "alocacao", schema = "sarc_allocations")
public class Alocacao {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;

    @Column(nullable = false, length = 120)
    private String disciplina;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "horario_inicio", nullable = false)
    private LocalTime horarioInicio;

    @Column(name = "horario_fim", nullable = false)
    private LocalTime horarioFim;

    @ManyToMany
    @JoinTable(
            name = "alocacao_recurso",
            schema = "sarc_allocations",
            joinColumns = @JoinColumn(name = "alocacao_id"),
            inverseJoinColumns = @JoinColumn(name = "recurso_id")
    )
    private Set<Recurso> recursos = new LinkedHashSet<>();

    protected Alocacao() {
    }

    public Long getId() {
        return id;
    }

    public Usuario getProfessor() {
        return professor;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public Set<Recurso> getRecursos() {
        return recursos;
    }
}
