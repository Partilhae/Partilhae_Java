package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.security.JwtUtils;
import br.com.partilhae.partilhae.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Os filtros estão desligados nesta fatia; a autorização real é exercitada nos testes de integração.
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean JwtUtils jwtUtils;

    @AfterEach
    void limparSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /auth/login retorna JWT e dados do perfil autenticado")
    void loginValido() throws Exception {
        UserDetailsImpl usuario = new UserDetailsImpl(1L, "Doador Teste", "doador@partilhae.test",
                "hash", 10L, null, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_DOADOR")));
        var autenticacao = UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(autenticacao);
        when(jwtUtils.generateJwtToken(autenticacao)).thenReturn("jwt-de-teste");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"doador@partilhae.test","senha":"Senha@123"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt-de-teste"))
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.role").value("DOADOR"))
                .andExpect(jsonPath("$.idUsuario").value(1)).andExpect(jsonPath("$.idDoador").value(10));
    }

    @Test
    @DisplayName("POST /auth/login rejeita corpo incompleto ou malformado sem autenticar")
    void loginInvalido() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"","senha":""}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verify(authenticationManager, never()).authenticate(any());
    }
}
