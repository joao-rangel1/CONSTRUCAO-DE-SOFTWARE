package br.edu.sarc.resource.controller;

import br.edu.sarc.resource.dto.ErrorResponse;
import br.edu.sarc.resource.dto.RecursoRequest;
import br.edu.sarc.resource.dto.RecursoResponse;
import br.edu.sarc.resource.service.RecursoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Recursos", description = "Gestao de salas, laboratorios e equipamentos do SARC")
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping("/public")
    @Operation(
            summary = "Lista recursos ativos publicamente",
            description = "Retorna apenas recursos ativos para consultas publicas. Nao exige autenticacao."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Recursos ativos encontrados",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecursoResponse.class)))
    )
    public List<RecursoResponse> listarAtivosPublicamente() {
        return recursoService.listarAtivosPublicamente();
    }

    @GetMapping("/public/{id}")
    @Operation(
            summary = "Busca recurso ativo publicamente",
            description = "Consulta um recurso ativo pelo identificador. Recursos inativos nao sao retornados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso ativo encontrado", content = @Content(schema = @Schema(implementation = RecursoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso ativo nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RecursoResponse buscarAtivoPublicamente(
            @Parameter(description = "Identificador do recurso", example = "1", required = true)
            @PathVariable("id") Long id
    ) {
        return recursoService.buscarAtivoPublicamente(id);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lista todos os recursos",
            description = "Lista recursos ativos e inativos. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recursos encontrados"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public Page<RecursoResponse> listarTodos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable
    ) {
        return recursoService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Busca recurso por id",
            description = "Consulta recurso ativo ou inativo pelo identificador. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso encontrado", content = @Content(schema = @Schema(implementation = RecursoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Recurso nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RecursoResponse buscarPorId(
            @Parameter(description = "Identificador do recurso", example = "1", required = true)
            @PathVariable("id") Long id
    ) {
        return recursoService.buscarPorId(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cria recurso",
            description = "Cria sala, laboratorio, computador, projetor ou equipamento. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recurso criado", content = @Content(schema = @Schema(implementation = RecursoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<RecursoResponse> criar(@Valid @RequestBody RecursoRequest request) {
        RecursoResponse response = recursoService.criar(request);
        return ResponseEntity
                .created(URI.create("/api/resources/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Atualiza recurso",
            description = "Atualiza dados cadastrais e opcionalmente o status ativo. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso atualizado", content = @Content(schema = @Schema(implementation = RecursoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Recurso nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RecursoResponse atualizar(
            @Parameter(description = "Identificador do recurso", example = "1", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody RecursoRequest request
    ) {
        return recursoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/activate")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ativa recurso", description = "Marca um recurso como ativo. Disponivel apenas para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso ativado", content = @Content(schema = @Schema(implementation = RecursoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Recurso nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RecursoResponse ativar(
            @Parameter(description = "Identificador do recurso", example = "1", required = true)
            @PathVariable("id") Long id
    ) {
        return recursoService.ativar(id);
    }

    @PatchMapping("/{id}/deactivate")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desativa recurso", description = "Marca um recurso como inativo. Disponivel apenas para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso desativado", content = @Content(schema = @Schema(implementation = RecursoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Recurso nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RecursoResponse desativar(
            @Parameter(description = "Identificador do recurso", example = "1", required = true)
            @PathVariable("id") Long id
    ) {
        return recursoService.desativar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove recurso", description = "Remove um recurso. Disponivel apenas para ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Recurso removido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Recurso nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void remover(
            @Parameter(description = "Identificador do recurso", example = "1", required = true)
            @PathVariable("id") Long id
    ) {
        recursoService.remover(id);
    }
}
