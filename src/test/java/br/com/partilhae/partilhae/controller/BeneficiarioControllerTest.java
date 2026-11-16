package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.BeneficiarioRequestDTO;
import br.com.partilhae.partilhae.dto.BeneficiarioResponseDTO;
import br.com.partilhae.partilhae.dto.BeneficiarioUpdateRequestDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.service.BeneficiarioService;
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
@WebMvcTest(BeneficiarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BeneficiarioControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean BeneficiarioService beneficiarioService;
    private final BeneficiarioResponseDTO beneficiario = new BeneficiarioResponseDTO(20L, 2L, "Beneficiário Teste",
            "beneficiario@partilhae.test", "11999999999", "01001000", "Rua de Teste", "10", null,
            "Centro", "São Paulo", "SP", 4, false, "Sem lactose", "Contato à tarde",
            "pessoaFisica", null);

    @Test
    @DisplayName("POST /beneficiarios mantém o cadastro aceito pelo aplicativo Mobile")
    void cadastroCompativel() throws Exception {
        when(beneficiarioService.cadastrar(any(BeneficiarioRequestDTO.class))).thenReturn(beneficiario);
        mockMvc.perform(post("/beneficiarios").contentType(MediaType.APPLICATION_JSON).content(corpoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idReceptor").value(20))
                .andExpect(jsonPath("$.possuiCriancas").value(false));
    }

    @Test
    @DisplayName("POST /beneficiarios não chama o service quando os campos são inválidos")
    void cadastroInvalido() throws Exception {
        mockMvc.perform(post("/beneficiarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"sem-nome"}
                                """))
                .andExpect(status().isBadRequest());
        verify(beneficiarioService, never()).cadastrar(any());
    }

    @Test
    @DisplayName("GET /beneficiarios/criancas e /restricao-alimentar passam filtros e paginação")
    void buscasAdministrativas() throws Exception {
        PageRequest pageable = PageRequest.of(0, 5);
        when(beneficiarioService.buscarPorPossuiCriancas("N", pageable))
                .thenReturn(new PageImpl<>(List.of(beneficiario), pageable, 1));
        mockMvc.perform(get("/beneficiarios/criancas?possuiCriancas=N&page=0&size=5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].possuiCriancas").value(false));
        when(beneficiarioService.buscarPorRestricaoAlimentar("lactose", pageable))
                .thenReturn(new PageImpl<>(List.of(beneficiario), pageable, 1));
        mockMvc.perform(get("/beneficiarios/restricao-alimentar?termo=lactose&page=0&size=5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].idReceptor").value(20));
    }

    @Test
    @DisplayName("Parâmetros de busca ausentes ou inválidos retornam 400 sem chamar o service")
    void parametrosInvalidos() throws Exception {
        mockMvc.perform(get("/beneficiarios/criancas?possuiCriancas=talvez"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/beneficiarios/restricao-alimentar?termo="))
                .andExpect(status().isBadRequest());
        verify(beneficiarioService, never()).buscarPorPossuiCriancas(any(), any());
        verify(beneficiarioService, never()).buscarPorRestricaoAlimentar(any(), any());
    }

    @Test
    @DisplayName("GET /beneficiarios/{id} retorna dados e traduz perfil inexistente para 404")
    void consultaPerfil() throws Exception {
        when(beneficiarioService.buscarPorId(20L)).thenReturn(beneficiario);
        mockMvc.perform(get("/beneficiarios/20")).andExpect(status().isOk())
                .andExpect(jsonPath("$.idReceptor").value(20));
        when(beneficiarioService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Receptor não encontrado"));
        mockMvc.perform(get("/beneficiarios/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /beneficiarios respeita paginação")
    void listaPaginada() throws Exception {
        PageRequest pageable = PageRequest.of(1, 3);
        when(beneficiarioService.listar(pageable)).thenReturn(new PageImpl<>(List.of(beneficiario), pageable, 4));
        mockMvc.perform(get("/beneficiarios?page=1&size=3"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    @DisplayName("PUT /beneficiarios/{id} não chama o service para atualização vazia")
    void atualizacaoVazia() throws Exception {
        mockMvc.perform(put("/beneficiarios/20").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(beneficiarioService, never()).atualizar(anyLong(), any(BeneficiarioUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /beneficiarios/{id} desativa perfil com resposta 204")
    void desativacao() throws Exception {
        mockMvc.perform(delete("/beneficiarios/20")).andExpect(status().isNoContent());
        verify(beneficiarioService).desativar(20L);
    }

    private static String corpoValido() {
        return """
                {"nomeUsuario":"Maria Oliveira","email":"maria@partilhae.test","senha":"Senha@123","cpf":"52998224725","telefone":"11999999999","cep":"01001000","endereco":"Rua de Teste","numero":"10","bairro":"Centro","cidade":"São Paulo","estado":"SP","quantidadePessoasFamilia":4,"possuiCriancas":false,"tipoBeneficiario":"pessoaFisica"}
                """;
    }
}
