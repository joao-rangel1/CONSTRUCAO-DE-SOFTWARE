package br.edu.sarc.user.dto;

import br.edu.sarc.user.domain.PerfilUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criacao de usuario interno do SARC")
public record UsuarioCreateRequest(
        @NotBlank
        @Size(max = 120)
        @Schema(description = "Nome completo do usuario", example = "Professor Teste")
        String nome,

        @NotBlank
        @Email
        @Size(max = 200)
        @Schema(description = "E-mail institucional do usuario", example = "professor@sarc.local")
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
                message = "A senha deve ter no minimo 8 caracteres, uma letra maiuscula e um numero"
        )
        @Schema(description = "Senha inicial. A API armazena apenas o hash.", example = "Sarc@2024")
        String senha,

        @NotNull
        @Schema(description = "Perfil interno permitido.", example = "PROFESSOR")
        PerfilUsuario perfil
) {
}
