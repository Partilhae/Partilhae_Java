package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.ReceptorRequestDTO;
import br.com.partilhae.partilhae.dto.ReceptorResponseDTO;
import br.com.partilhae.partilhae.service.ReceptorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Os filtros estão desligados nesta fatia; a autorização real é exercitada nos testes de integração.
@WebMvcTest(BeneficiarioCompatibilityController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BeneficiarioCompatibilityControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ReceptorService receptorService;

    @Test
    @DisplayName("POST /beneficiarios mantém o caminho de cadastro usado pelo Mobile")
    void cadastroCompativel() throws Exception {
        when(receptorService.cadastrar(any(ReceptorRequestDTO.class))).thenReturn(
                new ReceptorResponseDTO(20L, 2L, "Receptor Teste", "receptor@partilhae.test",
                        "11999999999", "01001000", "Rua de Teste", "10", null,
                        "Centro", "São Paulo", "SP", 4, false, null, null,
                        "pessoaFisica", null));
        mockMvc.perform(post("/beneficiarios").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoValido()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idReceptor").value(20))
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
        verify(receptorService, never()).cadastrar(any());
    }

    private static String corpoValido() {
        return """
                {"nomeUsuario":"Receptor Teste","email":"receptor@partilhae.test","senha":"Senha@123","cpf":"52998224725","telefone":"11999999999","cep":"01001000","endereco":"Rua de Teste","numero":"10","bairro":"Centro","cidade":"São Paulo","estado":"SP","quantidadePessoasFamilia":4,"possuiCriancas":false,"tipoBeneficiario":"pessoaFisica"}
                """;
    }
}
