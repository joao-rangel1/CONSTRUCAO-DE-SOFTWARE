package br.edu.sarc.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Projeção somente-leitura do recurso gerenciado pelo resource-service (schema sarc_resources).
 * O schedule-service NÃO gerencia nem altera dados de recurso.
 */
@Entity
@Immutable
@Table(name = "recurso", schema = "sarc_resources")
public class Recurso {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoRecurso tipo;

    @Column(length = 100)
    private String localizacao;

    @Column(nullable = false)
    private boolean ativo;

    protected Recurso() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
