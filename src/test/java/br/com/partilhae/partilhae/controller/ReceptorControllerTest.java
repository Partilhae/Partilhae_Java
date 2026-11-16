package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.ReceptorResponseDTO;
import br.com.partilhae.partilhae.dto.ReceptorUpdateRequestDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.service.ReceptorService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Os filtros estão desligados nesta fatia; a autorização real é exercitada nos testes de integração.
@WebMvcTest(ReceptorController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReceptorControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ReceptorService receptorService;
    private final ReceptorResponseDTO receptor = new ReceptorResponseDTO(20L, 2L, "Receptor Teste",
            "receptor@partilhae.test", "11999999999", "01001000", "Rua de Teste", "10", null,
            "Centro", "São Paulo", "SP", 4, false, "Sem lactose", "Contato à tarde",
            "pessoaFisica", null);

    @Test
    @DisplayName("GET /receptores/criancas e /restricao-alimentar passam filtros e paginação")
    void buscasAdministrativas() throws Exception {
        PageRequest pageable = PageRequest.of(0, 5);
        when(receptorService.buscarPorPossuiCriancas("N", pageable))
                .thenReturn(new PageImpl<>(List.of(receptor), pageable, 1));
        mockMvc.perform(get("/receptores/criancas?possuiCriancas=N&page=0&size=5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].possuiCriancas").value(false));
        when(receptorService.buscarPorRestricaoAlimentar("lactose", pageable))
                .thenReturn(new PageImpl<>(List.of(receptor), pageable, 1));
        mockMvc.perform(get("/receptores/restricao-alimentar?termo=lactose&page=0&size=5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].idReceptor").value(20));
    }

    @Test
    @DisplayName("Parâmetros de busca ausentes ou inválidos retornam 400 sem chamar o service")
    void parametrosInvalidos() throws Exception {
        mockMvc.perform(get("/receptores/criancas?possuiCriancas=talvez"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/receptores/restricao-alimentar?termo="))
                .andExpect(status().isBadRequest());
        verify(receptorService, never()).buscarPorPossuiCriancas(any(), any());
        verify(receptorService, never()).buscarPorRestricaoAlimentar(any(), any());
    }

    @Test
    @DisplayName("GET /receptores/{id} retorna dados e traduz perfil inexistente para 404")
    void consultaPerfil() throws Exception {
        when(receptorService.buscarPorId(20L)).thenReturn(receptor);
        mockMvc.perform(get("/receptores/20")).andExpect(status().isOk())
                .andExpect(jsonPath("$.idReceptor").value(20));
        when(receptorService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Receptor não encontrado"));
        mockMvc.perform(get("/receptores/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /receptores respeita paginação")
    void listaPaginada() throws Exception {
        PageRequest pageable = PageRequest.of(1, 3);
        when(receptorService.listar(pageable)).thenReturn(new PageImpl<>(List.of(receptor), pageable, 4));
        mockMvc.perform(get("/receptores?page=1&size=3"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    @DisplayName("PUT /receptores/{id} não chama o service para atualização vazia")
    void atualizacaoVazia() throws Exception {
        mockMvc.perform(put("/receptores/20").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(receptorService, never()).atualizar(anyLong(), any(ReceptorUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /receptores/{id} desativa perfil com resposta 204")
    void desativacao() throws Exception {
        mockMvc.perform(delete("/receptores/20")).andExpect(status().isNoContent());
        verify(receptorService).desativar(20L);
    }
}
