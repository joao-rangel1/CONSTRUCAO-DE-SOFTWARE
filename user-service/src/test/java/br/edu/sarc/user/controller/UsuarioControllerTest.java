package br.edu.sarc.user.controller;

import br.edu.sarc.user.config.SecurityConfig;
import br.edu.sarc.user.domain.PerfilUsuario;
import br.edu.sarc.user.dto.UsuarioResponse;
import br.edu.sarc.user.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void deveListarProfessoresPublicamente() throws Exception {
        when(usuarioService.listarProfessores()).thenReturn(List.of(
                new UsuarioResponse(1L, "Professor Teste", "professor@sarc.local", PerfilUsuario.PROFESSOR, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/users/professors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Professor Teste"))
                .andExpect(jsonPath("$[0].perfil").value("PROFESSOR"));
    }

    @Test
    void naoDeveExporSenhaHashNasRespostas() throws Exception {
        when(usuarioService.listarProfessores()).thenReturn(List.of(
                new UsuarioResponse(1L, "Professor Teste", "professor@sarc.local", PerfilUsuario.PROFESSOR, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/users/professors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", not(hasKey("senhaHash"))))
                .andExpect(jsonPath("$[0]", not(hasKey("senha_hash"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarUsuarioComoAdmin() throws Exception {
        when(usuarioService.criar(any())).thenReturn(
                new UsuarioResponse(3L, "Novo Professor", "novo.professor@sarc.local", PerfilUsuario.PROFESSOR, LocalDateTime.now())
        );

        String body = """
                {
                  "nome": "Novo Professor",
                  "email": "novo.professor@sarc.local",
                  "senha": "Sarc@2024",
                  "perfil": "PROFESSOR"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("novo.professor@sarc.local"))
                .andExpect(jsonPath("$", not(hasKey("senhaHash"))))
                .andExpect(jsonPath("$", not(hasKey("senha_hash"))));
    }
}
