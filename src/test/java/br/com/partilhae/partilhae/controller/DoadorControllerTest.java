package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.DoadorRequestDTO;
import br.com.partilhae.partilhae.dto.DoadorResponseDTO;
import br.com.partilhae.partilhae.dto.DoadorUpdateRequestDTO;
import br.com.partilhae.partilhae.exception.DadoDuplicadoException;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.service.DoadorService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Os filtros estão desligados nesta fatia; a autorização real é exercitada nos testes de integração.
@WebMvcTest(DoadorController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DoadorControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean DoadorService doadorService;
    private final DoadorResponseDTO doador = new DoadorResponseDTO(10L, 1L, "Doador Teste",
            "doador@partilhae.test", "11999999999", "01001000", "Rua de Teste", "10", null,
            "Centro", "São Paulo", "SP", "Finais de semana", "Retirada combinada",
            "pessoaFisica", null);

    @Test
    @DisplayName("POST /doadores retorna 201 e o perfil criado")
    void cadastroValido() throws Exception {
        when(doadorService.cadastrar(any(DoadorRequestDTO.class))).thenReturn(doador);
        mockMvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(corpoValido()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idDoador").value(10))
                .andExpect(jsonPath("$.nomeUsuario").value("Doador Teste"));
    }

    @Test
    @DisplayName("POST /doadores rejeita campos inválidos e JSON malformado sem chamar o service")
    void cadastroInvalido() throws Exception {
        mockMvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeUsuario\":\"\",\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields").exists());
        mockMvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verify(doadorService, never()).cadastrar(any());
    }

    @Test
    @DisplayName("Cadastro de e-mail duplicado retorna erro padronizado 409")
    void cadastroDuplicado() throws Exception {
        when(doadorService.cadastrar(any(DoadorRequestDTO.class)))
                .thenThrow(new DadoDuplicadoException("E-mail já cadastrado"));
        mockMvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(corpoValido()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    @DisplayName("GET /doadores/{id} retorna perfil existente ou 404")
    void consultaPerfil() throws Exception {
        when(doadorService.buscarPorId(10L)).thenReturn(doador);
        mockMvc.perform(get("/doadores/10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1));
        when(doadorService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Doador não encontrado"));
        mockMvc.perform(get("/doadores/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /doadores respeita parâmetros de paginação")
    void listaPaginada() throws Exception {
        when(doadorService.listar(PageRequest.of(1, 2)))
                .thenReturn(new PageImpl<>(List.of(doador), PageRequest.of(1, 2), 5));
        mockMvc.perform(get("/doadores?page=1&size=2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].idDoador").value(10))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("PUT /doadores/{id} rejeita objeto vazio antes de chamar o service")
    void atualizacaoSemCampos() throws Exception {
        mockMvc.perform(put("/doadores/10").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(doadorService, never()).atualizar(anyLong(), any(DoadorUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /doadores/{id} desativa perfil e retorna 204")
    void desativacao() throws Exception {
        mockMvc.perform(delete("/doadores/10")).andExpect(status().isNoContent());
        verify(doadorService).desativar(10L);
    }

    private static String corpoValido() {
        return """
                {"nomeUsuario":"Doador Teste","email":"doador@partilhae.test","senha":"Senha@123","cpf":"52998224725","telefone":"11999999999","cep":"01001000","endereco":"Rua de Teste","numero":"10","bairro":"Centro","cidade":"São Paulo","estado":"SP","tipoDoador":"pessoaFisica"}
                """;
    }
}
