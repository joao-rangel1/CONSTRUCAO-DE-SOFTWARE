package br.edu.sarc.allocation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Dados para criacao ou atualizacao de alocacao")
public record AlocacaoRequest(

        /**
         * Regra de negócio para professorId:
         * - ADMIN: pode informar qualquer professorId válido; se omitido, usa o id do próprio usuário autenticado.
         * - PROFESSOR: deve omitir este campo (null) — o sistema usará automaticamente o id do professor autenticado.
         *   Se informado, deve ser igual ao próprio id; caso contrário, HTTP 403 é retornado.
         */
        @Schema(
                description = "ID do professor vinculado à alocação. " +
                        "ADMIN: pode informar qualquer professor; se omitido, usa o próprio id. " +
                        "PROFESSOR: deve omitir (null) — o sistema usa o id do token JWT automaticamente. " +
                        "Enviar o id de outro professor resulta em HTTP 403.",
                example = "1",
                nullable = true
        )
        Long professorId,

        @NotBlank
        @Size(max = 120)
        @Schema(description = "Disciplina da alocacao", example = "Engenharia de Software")
        String disciplina,

        @NotNull
        @FutureOrPresent
        @Schema(description = "Data da alocacao", example = "2026-05-22")
        LocalDate data,

        @NotNull
        @Schema(description = "Horario de inicio", example = "08:00")
        LocalTime horarioInicio,

        @NotNull
        @Schema(description = "Horario de fim (deve ser posterior ao inicio)", example = "10:00")
        LocalTime horarioFim,

        @NotEmpty
        @Schema(description = "Lista de IDs dos recursos alocados (minimo 1, sem duplicatas)", example = "[1, 4]")
        List<Long> recursoIds
) {
}
