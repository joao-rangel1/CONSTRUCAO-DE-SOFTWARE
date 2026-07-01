package br.edu.sarc.allocation.controller;

import br.edu.sarc.allocation.dto.AlocacaoPublicResponse;
import br.edu.sarc.allocation.dto.AlocacaoRequest;
import br.edu.sarc.allocation.dto.AlocacaoResponse;
import br.edu.sarc.common.dto.ErrorResponse;
import br.edu.sarc.allocation.service.AlocacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@Tag(name = "Alocacoes", description = "Criacao, consulta, edicao e remocao de alocacoes do SARC")
public class AlocacaoController {

    private final AlocacaoService alocacaoService;

    public AlocacaoController(AlocacaoService alocacaoService) {
        this.alocacaoService = alocacaoService;
    }

    @GetMapping("/public")
    @Operation(
            summary = "Consulta alocacoes publicamente",
            description = "Lista alocacoes com filtros opcionais por data, professor, disciplina e recurso. Nao exige autenticacao."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Alocacoes encontradas",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlocacaoPublicResponse.class)))
    )
    public List<AlocacaoPublicResponse> buscarPublicamente(
            @Parameter(description = "Data da alocacao", example = "2026-05-22")
            @RequestParam(name = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "Id do professor", example = "1")
            @RequestParam(name = "professor", required = false) Long professor,
            @Parameter(description = "Trecho da disciplina", example = "Software")
            @RequestParam(name = "disciplina", required = false) String disciplina,
            @Parameter(description = "Id do recurso", example = "1")
            @RequestParam(name = "recurso", required = false) Long recurso
    ) {
        return alocacaoService.buscarPublicamente(data, professor, disciplina, recurso);
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Consulta alocacao publicamente", description = "Consulta uma alocacao por id sem exigir autenticacao.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alocacao encontrada", content = @Content(schema = @Schema(implementation = AlocacaoPublicResponse.class))),
            @ApiResponse(responseCode = "404", description = "Alocacao nao encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AlocacaoPublicResponse buscarPublicamentePorId(
            @Parameter(description = "Identificador da alocacao", example = "1", required = true)
            @PathVariable("id") Long id
    ) {
        return alocacaoService.buscarPublicamentePorId(id);
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista minhas alocacoes", description = "Lista alocacoes do professor autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alocacoes encontradas"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public List<AlocacaoResponse> listarMinhas() {
        return alocacaoService.listarMinhas();
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cria alocacao", description = "Cria alocacao com um ou mais recursos. Disponivel para PROFESSOR ou ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alocacao criada", content = @Content(schema = @Schema(implementation = AlocacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Regra de negocio violada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AlocacaoResponse> criar(@Valid @RequestBody AlocacaoRequest request) {
        AlocacaoResponse response = alocacaoService.criar(request);
        return ResponseEntity.created(URI.create("/api/allocations/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualiza alocacao", description = "Professor atualiza apenas a propria alocacao. ADMIN atualiza qualquer uma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alocacao atualizada", content = @Content(schema = @Schema(implementation = AlocacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Alocacao nao encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Regra de negocio violada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AlocacaoResponse atualizar(
            @Parameter(description = "Identificador da alocacao", example = "1", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody AlocacaoRequest request
    ) {
        return alocacaoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove alocacao", description = "Professor remove apenas a propria alocacao. ADMIN remove qualquer uma.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alocacao removida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Alocacao nao encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void remover(
            @Parameter(description = "Identificador da alocacao", example = "1", required = true)
            @PathVariable("id") Long id
    ) {
        alocacaoService.remover(id);
    }
}
