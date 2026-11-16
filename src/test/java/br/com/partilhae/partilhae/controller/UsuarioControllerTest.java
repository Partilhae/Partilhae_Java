package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.UsuarioRequestDTO;
import br.com.partilhae.partilhae.dto.UsuarioResponseDTO;
import br.com.partilhae.partilhae.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Os filtros estão desligados nesta fatia; a autorização real é exercitada nos testes de integração.
@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UsuarioControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UsuarioService usuarioService;
    private final UsuarioResponseDTO usuario = new UsuarioResponseDTO(1L, "Usuário Teste",
            "usuario@partilhae.test", "52998224725", "11999999999", "01001000", "Rua de Teste",
            "10", null, "Centro", "São Paulo", "SP", "ATIVA");

    @Test
    @DisplayName("GET /usuarios/{id} retorna dados do usuário")
    void consultaUsuario() throws Exception {
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);
        mockMvc.perform(get("/usuarios/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1)).andExpect(jsonPath("$.email").value("usuario@partilhae.test"));
    }

    @Test
    @DisplayName("GET /usuarios encaminha página solicitada")
    void listaPaginada() throws Exception {
        PageRequest pageable = PageRequest.of(0, 2);
        when(usuarioService.listar(pageable)).thenReturn(new PageImpl<>(List.of(usuario), pageable, 1));
        mockMvc.perform(get("/usuarios?page=0&size=2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].idUsuario").value(1));
    }

    @Test
    @DisplayName("PUT /usuarios/{id} rejeita objeto vazio antes de chamar o service")
    void atualizacaoInvalida() throws Exception {
        mockMvc.perform(put("/usuarios/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(usuarioService, never()).atualizar(anyLong(), any(UsuarioRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} retorna 204 após exclusão lógica solicitada")
    void excluir() throws Exception {
        mockMvc.perform(delete("/usuarios/1")).andExpect(status().isNoContent());
        verify(usuarioService).excluir(1L);
    }

    @Test
    @DisplayName("PATCH /usuarios/{id}/status valida os status permitidos")
    void statusInvalido() throws Exception {
        mockMvc.perform(patch("/usuarios/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusConta\":\"PENDENTE\"}"))
                .andExpect(status().isBadRequest());
        verify(usuarioService, never()).atualizarStatus(anyLong(), any());
    }
}
