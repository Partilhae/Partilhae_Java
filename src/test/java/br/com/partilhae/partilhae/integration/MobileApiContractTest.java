package br.com.partilhae.partilhae.integration;

import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.DenunciaRepository;
import br.com.partilhae.partilhae.repository.DoadorRepository;
import br.com.partilhae.partilhae.repository.BeneficiarioRepository;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.support.DadosDeTeste;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.jwtSecret=integration-test-secret-with-at-least-32-bytes"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MobileApiContractTest {
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired DoadorRepository doadorRepository;
    @Autowired BeneficiarioRepository beneficiarioRepository;
    @Autowired DenunciaRepository denunciaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void limpar() {
        denunciaRepository.deleteAll();
        doadorRepository.deleteAll();
        beneficiarioRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void cadastroDoadorLoginEAtualizacaoParcialPreservamContratoMobile() throws Exception {
        JsonNode created = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("maria@partilhae.test", "52998224725", "estabelecimento", "Padaria da Maria")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idDoador").isNumber())
                .andExpect(jsonPath("$.idUsuario").isNumber()).andExpect(jsonPath("$.nomeEstabelecimento").value("Padaria da Maria"))
                .andExpect(jsonPath("$.numero").value("100")).andExpect(jsonPath("$.complemento").value("Sala 2"))
                .andExpect(jsonPath("$.senha").doesNotExist()).andReturn().getResponse().getContentAsString());
        long doadorId = created.get("idDoador").asLong();
        long userId = created.get("idUsuario").asLong();
        String storedHash = usuarioRepository.findById(userId).orElseThrow().getSenha();
        assertNotEquals("Senha@123", storedHash);
        assertTrue(encoder.matches("Senha@123", storedHash));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson("maria@partilhae.test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipoToken").value("Bearer")).andExpect(jsonPath("$.idUsuario").value(userId))
                .andExpect(jsonPath("$.idDoador").value(doadorId)).andExpect(jsonPath("$.idReceptor").value(nullValue()))
                .andExpect(jsonPath("$.nomeUsuario").value("Doador Teste")).andExpect(jsonPath("$.email").value("maria@partilhae.test"))
                .andExpect(jsonPath("$.role").value("DOADOR"));

        String token = token("maria@partilhae.test");
        mvc.perform(put("/doadores/{id}", doadorId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"52998224725\",\"disponibilidade\":\"Todos os dias\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.disponibilidade").value("Todos os dias"));
        Usuario updatedUser = usuarioRepository.findById(userId).orElseThrow();
        assertEquals("01001000", updatedUser.getCep());
        assertEquals("100", updatedUser.getNumero());
    }

    @Test
    void cadastroBeneficiarioAceitaCamposComplementaresELoginReceptor() throws Exception {
        JsonNode created = json(mvc.perform(post("/beneficiarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeUsuario":"João Oliveira","email":"joao@partilhae.test","cpf":"11144477735","telefone":"11988888888","senha":"Senha@123","cep":"01310100","endereco":"Avenida Paulista","numero":"500","complemento":"","bairro":"Bela Vista","cidade":"São Paulo","estado":"SP","tipoBeneficiario":"ONG","nomeOrganizacao":"Projeto Esperança","quantidadePessoasFamilia":4,"possuiCriancas":true,"restricaoAlimentar":"Sem lactose","observacao":"Contato à tarde","raioBuscaKm":10}
                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.tipoBeneficiario").value("ONG"))
                .andExpect(jsonPath("$.nomeOrganizacao").value("Projeto Esperança"))
                .andExpect(jsonPath("$.quantidadePessoasFamilia").value(4)).andExpect(jsonPath("$.possuiCriancas").value(true))
                .andExpect(jsonPath("$.restricaoAlimentar").value("Sem lactose"))
                .andExpect(jsonPath("$.complemento").doesNotExist()).andReturn().getResponse().getContentAsString());
        assertEquals(Boolean.TRUE, beneficiarioRepository.findById(created.get("idReceptor").asLong())
                .orElseThrow().getPossuiCriancas());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson("joao@partilhae.test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.idDoador").value(nullValue()))
                .andExpect(jsonPath("$.idReceptor").value(created.get("idReceptor").asLong())).andExpect(jsonPath("$.role").value("RECEPTOR"));
        String token = token("joao@partilhae.test");
        long receptorId = created.get("idReceptor").asLong();
        mvc.perform(put("/beneficiarios/{id}", receptorId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"11144477735\",\"possuiCriancas\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.possuiCriancas").value(false));
        assertEquals(Boolean.FALSE, beneficiarioRepository.findById(receptorId).orElseThrow().getPossuiCriancas());
        mvc.perform(post("/receptores").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void cadastroAceitaCpfFicticio() throws Exception {
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("cpf-ficticio@partilhae.test", "12345678901", "pessoaFisica", null)))
                .andExpect(status().isCreated());
        assertEquals("12345678901", usuarioRepository.findByEmailIgnoreCase("cpf-ficticio@partilhae.test")
                .orElseThrow().getCpf());
    }

    @Test
    void cadastroRejeitaCamposObrigatoriosETiposInconsistentesEMantemDuplicidade() throws Exception {
        mvc.perform(post("/beneficiarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeUsuario":"ONG","email":"ong@partilhae.test","cpf":"52998224725","telefone":"11999999999","senha":"Senha@123","cep":"01001000","endereco":"Rua A","numero":"1","bairro":"Centro","cidade":"São Paulo","estado":"SP","tipoBeneficiario":"ONG"}
                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("missing-name@partilhae.test", "52998224725", "estabelecimento", null)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("invalid-type@partilhae.test", "52998224725", "empresa", "Empresa")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("dup@partilhae.test", "52998224725", "pessoaFisica", null)))
                .andExpect(status().isCreated());
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("dup@partilhae.test", "11144477735", "pessoaFisica", null)))
                .andExpect(status().isConflict());
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("dup-cpf@partilhae.test", "52998224725", "pessoaFisica", null)))
                .andExpect(status().isConflict());
        assertEquals(1, usuarioRepository.count());
    }

    @Test
    void loginBloqueadoJwtInvalidoERecursoSemTokenSaoNegados() throws Exception {
        JsonNode created = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("blocked@partilhae.test", "52998224725", "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long userId = created.get("idUsuario").asLong();
        Usuario blocked = usuarioRepository.findById(userId).orElseThrow();
        blocked.setStatusConta("BLOQUEADA");
        usuarioRepository.save(blocked);
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson("blocked@partilhae.test")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/usuarios/{id}", userId).header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/usuarios/{id}", userId)).andExpect(status().isUnauthorized());
        SecretKey key = Keys.hmacShaKeyFor("integration-test-secret-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String expired = Jwts.builder().subject("blocked@partilhae.test").issuedAt(Date.from(now.minusSeconds(600)))
                .expiration(Date.from(now.minusSeconds(300))).signWith(key).compact();
        mvc.perform(get("/usuarios/{id}", userId).header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void titularidadeAdministradorPaginacaoEExclusaoLogicaDeUsuario() throws Exception {
        JsonNode first = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("owner@partilhae.test", "52998224725", "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        JsonNode second = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("other@partilhae.test", "11144477735", "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        provisionarAdministrador();
        String ownerToken = token("owner@partilhae.test");
        String adminToken = token("admin@partilhae.test");
        mvc.perform(get("/usuarios/{id}", second.get("idUsuario").asLong()).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
        mvc.perform(put("/usuarios/{id}", second.get("idUsuario").asLong()).header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"11144477735\",\"nomeUsuario\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/usuarios/{id}", first.get("idUsuario").asLong()).header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"52998224725\",\"numero\":\"88\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numero").value("88"));
        mvc.perform(get("/usuarios").header("Authorization", "Bearer " + ownerToken)).andExpect(status().isForbidden());
        mvc.perform(get("/doadores").header("Authorization", "Bearer " + ownerToken)).andExpect(status().isForbidden());
        mvc.perform(get("/beneficiarios").header("Authorization", "Bearer " + ownerToken)).andExpect(status().isForbidden());
        mvc.perform(get("/beneficiarios/criancas?possuiCriancas=S").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/usuarios?page=0&size=1").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.totalPages").value(3));
        mvc.perform(patch("/usuarios/{id}/status", second.get("idUsuario").asLong()).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"statusConta\":\"BLOQUEADA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statusConta").value("BLOQUEADA"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson("other@partilhae.test")))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/usuarios/{id}", first.get("idUsuario").asLong()).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
        assertEquals(3, usuarioRepository.count());
        assertEquals("BLOQUEADA", usuarioRepository.findById(first.get("idUsuario").asLong())
                .orElseThrow().getStatusConta());
    }

    @Test
    void denunciasExigemUmAlvoUsamJwtEArmazenamAlvosExternosSemIntegracao() throws Exception {
        JsonNode reporter = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("reporter@partilhae.test", "52998224725", "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        JsonNode target = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content(doadorJson("target@partilhae.test", "11144477735", "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String token = token("reporter@partilhae.test");
        String report = "{\"idUsuarioDenunciado\":%d,\"idDenunciante\":999,\"motivo\":\"Relato objetivo.\"}".formatted(target.get("idUsuario").asLong());
        JsonNode savedReport = json(mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(report))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idDenunciante").value(reporter.get("idUsuario").asLong()))
                .andExpect(jsonPath("$.idUsuarioDenunciado").value(target.get("idUsuario").asLong())).andExpect(jsonPath("$.status").value("ABERTA"))
                .andReturn().getResponse().getContentAsString());
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuarioDenunciado\":%d,\"idDoacao\":2,\"motivo\":\"Dois alvos\"}".formatted(target.get("idUsuario").asLong())))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idDoacao\":2,\"motivo\":\"Doação\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idDoacao").value(2));
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idConversa\":2,\"motivo\":\"Conversa\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idConversa").value(2));
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idSolicitacao\":2,\"motivo\":\"Solicitação\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idSolicitacao").value(2));
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Sem alvo\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuarioDenunciado\":1,\"idConversa\":2,\"idSolicitacao\":3,\"idDoacao\":4,\"motivo\":\"Quatro alvos\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuarioDenunciado\":%d,\"motivo\":\"autodenúncia\"}".formatted(reporter.get("idUsuario").asLong())))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/denuncias").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuarioDenunciado\":999999,\"motivo\":\"Usuário ausente\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/denuncias").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(get("/denuncias/{id}", savedReport.get("idDenuncia").asLong()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/denuncias/usuario/{id}", reporter.get("idUsuario").asLong()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(4));
        mvc.perform(get("/denuncias/usuario/{id}", target.get("idUsuario").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        provisionarAdministrador();
        String adminToken = token("admin@partilhae.test");
        mvc.perform(get("/denuncias").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.totalPages").isNumber());
        for (String newStatus : new String[]{"ABERTA", "EM_ANALISE", "RESOLVIDA", "REJEITADA"}) {
            mvc.perform(patch("/denuncias/{id}/status", savedReport.get("idDenuncia").asLong())
                            .header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"%s\"}".formatted(newStatus)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(newStatus));
        }
        mvc.perform(patch("/denuncias/{id}/resolucao", savedReport.get("idDenuncia").asLong())
                        .header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolucao\":\"Revisada pela equipe.\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resolucao").value("Revisada pela equipe."));
        mvc.perform(get("/denuncias/{id}", savedReport.get("idDenuncia").asLong())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVIDA"));
        mvc.perform(delete("/denuncias/{id}", savedReport.get("idDenuncia").asLong()).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void papeisNaoAlteramPerfisDeOutrosNemDeOutroTipo() throws Exception {
        JsonNode doadorA = cadastrarDoador("doador-a@partilhae.test", "52998224725");
        JsonNode doadorB = cadastrarDoador("doador-b@partilhae.test", "11144477735");
        JsonNode receptorA = cadastrarReceptor("receptor-a@partilhae.test", "93541134780");
        JsonNode receptorB = cadastrarReceptor("receptor-b@partilhae.test", "12345678909");
        String tokenDoadorA = token("doador-a@partilhae.test");
        String tokenReceptorA = token("receptor-a@partilhae.test");

        mvc.perform(put("/doadores/{id}", doadorB.get("idDoador").asLong())
                        .header("Authorization", "Bearer " + tokenDoadorA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"11144477735\",\"disponibilidade\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/beneficiarios/{id}", receptorA.get("idReceptor").asLong())
                        .header("Authorization", "Bearer " + tokenDoadorA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"93541134780\",\"observacao\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/beneficiarios/{id}", receptorB.get("idReceptor").asLong())
                        .header("Authorization", "Bearer " + tokenReceptorA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"12345678909\",\"observacao\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/doadores/{id}", doadorA.get("idDoador").asLong())
                        .header("Authorization", "Bearer " + tokenReceptorA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"52998224725\",\"disponibilidade\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void privacidadeDoPerfilEEnderecoEhAplicada() throws Exception {
        JsonNode receptor = json(mvc.perform(post("/beneficiarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeUsuario":"Receptor","email":"privacy-receptor@partilhae.test","cpf":"52998224725","telefone":"11988888888","senha":"Senha@123","cep":"01310100","endereco":"Avenida Paulista","numero":"500","bairro":"Bela Vista","cidade":"São Paulo","estado":"SP","tipoBeneficiario":"pessoaFisica","quantidadePessoasFamilia":3,"possuiCriancas":true,"restricaoAlimentar":"Privada"}
                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        JsonNode doador = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("privacy-doador@partilhae.test", "11144477735", "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        JsonNode outroDoador = json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("outro-doador@partilhae.test", "93541134780", "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String token = token("privacy-doador@partilhae.test");
        mvc.perform(get("/beneficiarios/{id}", receptor.get("idReceptor").asLong()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cpf").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.telefone").doesNotExist()).andExpect(jsonPath("$.cep").doesNotExist())
                .andExpect(jsonPath("$.endereco").doesNotExist()).andExpect(jsonPath("$.numero").doesNotExist())
                .andExpect(jsonPath("$.quantidadePessoasFamilia").doesNotExist())
                .andExpect(jsonPath("$.possuiCriancas").doesNotExist()).andExpect(jsonPath("$.restricaoAlimentar").doesNotExist());
        String receptorToken = token("privacy-receptor@partilhae.test");
        mvc.perform(get("/beneficiarios/{id}", receptor.get("idReceptor").asLong())
                        .header("Authorization", "Bearer " + receptorToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("privacy-receptor@partilhae.test"))
                .andExpect(jsonPath("$.telefone").value("11988888888"))
                .andExpect(jsonPath("$.quantidadePessoasFamilia").value(3));
        mvc.perform(get("/doadores/{id}", outroDoador.get("idDoador").asLong())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nomeUsuario").value("Doador Teste"))
                .andExpect(jsonPath("$.cpf").doesNotExist()).andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.telefone").doesNotExist())
                .andExpect(jsonPath("$.cep").doesNotExist()).andExpect(jsonPath("$.endereco").doesNotExist());
        provisionarAdministrador();
        String adminToken = token("admin@partilhae.test");
        mvc.perform(get("/doadores/{id}", doador.get("idDoador").asLong())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("privacy-doador@partilhae.test"))
                .andExpect(jsonPath("$.cep").value("01001000"));
    }

    private String token(String email) throws Exception {
        return json(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson(email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();
    }

    private Usuario provisionarAdministrador() {
        Usuario administrador = DadosDeTeste.usuario(null, "admin@partilhae.test", "ADMIN");
        administrador.setCpf(usuarioRepository.existsByCpf("93541134780")
                ? "12345678909" : "93541134780");
        administrador.setSenha(encoder.encode("Senha@123"));
        return usuarioRepository.save(administrador);
    }

    private JsonNode cadastrarDoador(String email, String cpf) throws Exception {
        return json(mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson(email, cpf, "pessoaFisica", null)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode cadastrarReceptor(String email, String cpf) throws Exception {
        return json(mvc.perform(post("/beneficiarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeUsuario":"Receptor Teste","email":"%s","cpf":"%s","telefone":"11988888888","senha":"Senha@123","cep":"01310100","endereco":"Avenida Paulista","numero":"500","bairro":"Bela Vista","cidade":"São Paulo","estado":"SP","tipoBeneficiario":"pessoaFisica"}
                """.formatted(email, cpf)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode json(String value) throws Exception { return objectMapper.readTree(value); }
    private static String loginJson(String email) { return "{\"email\":\"%s\",\"senha\":\"Senha@123\"}".formatted(email); }
    private static String doadorJson(String email, String cpf, String tipo, String estabelecimento) {
        String establishment = estabelecimento == null ? "null" : "\"" + estabelecimento + "\"";
        return "{\"nomeUsuario\":\"Doador Teste\",\"email\":\"%s\",\"cpf\":\"%s\",\"telefone\":\"11999999999\",\"senha\":\"Senha@123\",\"cep\":\"01001000\",\"endereco\":\"Praça da Sé\",\"numero\":\"100\",\"complemento\":\"Sala 2\",\"bairro\":\"Sé\",\"cidade\":\"São Paulo\",\"estado\":\"SP\",\"tipoDoador\":\"%s\",\"nomeEstabelecimento\":%s}".formatted(email, cpf, tipo, establishment);
    }
}
