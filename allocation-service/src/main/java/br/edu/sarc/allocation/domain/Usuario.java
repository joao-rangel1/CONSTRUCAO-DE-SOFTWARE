package br.edu.sarc.allocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Projeção somente-leitura do usuário gerenciado pelo user-service (schema sarc_users).
 * O allocation-service NÃO gerencia este dado — apenas o lê para associar professor às alocações.
 * Alterações no cadastro de usuários devem ser feitas via user-service.
 */
@Entity
@Immutable
@Table(name = "usuario", schema = "sarc_users")
public class Usuario {

    @Id
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 200)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    protected Usuario() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }
}
