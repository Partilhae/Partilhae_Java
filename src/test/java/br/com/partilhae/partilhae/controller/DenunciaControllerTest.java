package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.DenunciaRequestDTO;
import br.com.partilhae.partilhae.dto.DenunciaResponseDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.service.DenunciaService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Os filtros estão desligados nesta fatia; a autorização real é exercitada nos testes de integração.
@WebMvcTest(DenunciaController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DenunciaControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean DenunciaService denunciaService;
    private final DenunciaResponseDTO denuncia = new DenunciaResponseDTO(5L, 1L, 2L,
            null, null, null, "Relato objetivo", "ABERTA", LocalDateTime.of(2026, 1, 1, 10, 0), null);

    @Test
    @DisplayName("POST /denuncias retorna denúncia com status inicial")
    void cadastroValido() throws Exception {
        when(denunciaService.cadastrar(any(DenunciaRequestDTO.class))).thenReturn(denuncia);
        mockMvc.perform(post("/denuncias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuarioDenunciado\":2,\"motivo\":\"Relato objetivo\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idDenunciante").value(1))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    @DisplayName("POST /denuncias rejeita alvo ausente, alvos múltiplos e motivo vazio")
    void cadastroInvalido() throws Exception {
        mockMvc.perform(post("/denuncias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Sem alvo\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/denuncias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idDoacao\":3,\"idConversa\":4,\"motivo\":\"Dois alvos\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/denuncias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idDoacao\":3,\"motivo\":\" \"}"))
                .andExpect(status().isBadRequest());
        verify(denunciaService, never()).cadastrar(any());
    }

    @Test
    @DisplayName("GET /denuncias e GET /denuncias/usuario/{id} retornam páginas")
    void listagensPaginadas() throws Exception {
        PageRequest pageable = PageRequest.of(0, 5);
        when(denunciaService.listar(pageable)).thenReturn(new PageImpl<>(List.of(denuncia), pageable, 1));
        mockMvc.perform(get("/denuncias?page=0&size=5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].idDenuncia").value(5));
        when(denunciaService.listarPorUsuario(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(denuncia), pageable, 1));
        mockMvc.perform(get("/denuncias/usuario/1?page=0&size=5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /denuncias/{id} traduz denúncia inexistente para 404")
    void denunciaInexistente() throws Exception {
        when(denunciaService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Denúncia não encontrada"));
        mockMvc.perform(get("/denuncias/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH de status e resolução valida o corpo")
    void atualizacoesInvalidas() throws Exception {
        mockMvc.perform(patch("/denuncias/5/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDENTE\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/denuncias/5/resolucao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolucao\":\" \"}"))
                .andExpect(status().isBadRequest());
        verify(denunciaService, never()).atualizarStatus(anyLong(), any());
        verify(denunciaService, never()).atualizarResolucao(anyLong(), any());
    }

    @Test
    @DisplayName("DELETE /denuncias/{id} não possui endpoint de exclusão pública")
    void naoHaExclusaoPublica() throws Exception {
        mockMvc.perform(delete("/denuncias/5")).andExpect(status().isMethodNotAllowed());
        verify(denunciaService, never()).buscar(anyLong());
    }
}
