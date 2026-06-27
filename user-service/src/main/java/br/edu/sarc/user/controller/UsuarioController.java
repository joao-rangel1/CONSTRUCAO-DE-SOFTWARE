package br.edu.sarc.user.controller;

import br.edu.sarc.user.dto.ErrorResponse;
import br.edu.sarc.user.dto.UsuarioCreateRequest;
import br.edu.sarc.user.dto.UsuarioResponse;
import br.edu.sarc.user.dto.UsuarioUpdateRequest;
import br.edu.sarc.user.service.UsuarioService;
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
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Gestao de professores e administradores internos do SARC")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/professors")
    @Operation(
            summary = "Lista professores publicamente",
            description = "Retorna usuarios com perfil PROFESSOR para filtros da tela publica. Nao exige autenticacao."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Professores encontrados",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))
            )
    })
    public List<UsuarioResponse> listarProfessores() {
        return usuarioService.listarProfessores();
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lista usuarios internos",
            description = "Lista professores e administradores. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public Page<UsuarioResponse> listarTodos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable
    ) {
        return usuarioService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Busca usuario por id",
            description = "Consulta um usuario interno pelo identificador. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioResponse buscarPorId(
            @Parameter(description = "Identificador do usuario", example = "1", required = true)
            @PathVariable Long id
    ) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cria usuario interno",
            description = "Cria professor ou administrador. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario criado", content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse response = usuarioService.criar(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Atualiza usuario interno",
            description = "Atualiza professor ou administrador. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario atualizado", content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioResponse atualizar(
            @Parameter(description = "Identificador do usuario", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        return usuarioService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove usuario interno",
            description = "Remove um usuario interno. Disponivel apenas para ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario removido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void remover(
            @Parameter(description = "Identificador do usuario", example = "1", required = true)
            @PathVariable Long id
    ) {
        usuarioService.remover(id);
    }
}
