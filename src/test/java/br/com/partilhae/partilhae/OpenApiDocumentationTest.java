package br.com.partilhae.partilhae;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "delete", "patch");

    @Autowired MockMvc mockMvc;

    @Test
    void especificacaoDocumentaSecoesOperacoesParametrosERotasAtuais() throws Exception {
        String document = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode openApi = JsonMapper.builder().build().readTree(document);

        assertThat(openApi.path("info").path("title").asText()).isEqualTo("Partilhaê API");
        assertThat(openApi.path("info").path("version").asText()).isEqualTo("1.0.0");
        assertThat(openApi.path("tags").findValuesAsText("name")).containsExactlyInAnyOrder(
                "Autenticação", "Usuários", "Doadores", "Beneficiários", "Denúncias");
        assertThat(openApi.path("components").path("securitySchemes").path("bearerAuth").path("scheme").asText())
                .isEqualTo("bearer");
        assertThat(openApi.path("components").path("securitySchemes").path("bearerAuth").path("bearerFormat").asText())
                .isEqualTo("JWT");
        assertThat(openApi.path("security").isMissingNode()).isTrue();

        JsonNode paths = openApi.path("paths");
        assertThat(paths.has("/receptores")).isFalse();
        assertThat(paths.path("/beneficiarios").path("post").path("security").isMissingNode()).isTrue();
        assertThat(paths.path("/doadores").path("post").path("security").isMissingNode()).isTrue();
        assertThat(paths.path("/auth/login").path("post").path("security").isMissingNode()).isTrue();
        assertThat(paths.path("/beneficiarios").path("get").path("security").isArray()).isTrue();
        assertThat(paths.path("/denuncias").path("post").path("security").isArray()).isTrue();

        JsonNode beneficiarioIdParameters = paths.path("/beneficiarios/{id}").path("get").path("parameters");
        assertThat(findParameter(beneficiarioIdParameters, "id").path("description").asText())
                .isEqualTo("Identificador do beneficiário");
        JsonNode childrenParameters = paths.path("/beneficiarios/criancas").path("get").path("parameters");
        assertThat(findParameter(childrenParameters, "possuiCriancas").path("description").asText())
                .contains("S para sim ou N para não");

        int operationCount = 0;
        Iterator<Map.Entry<String, JsonNode>> pathEntries = paths.fields();
        while (pathEntries.hasNext()) {
            JsonNode pathItem = pathEntries.next().getValue();
            Iterator<Map.Entry<String, JsonNode>> operationEntries = pathItem.fields();
            while (operationEntries.hasNext()) {
                Map.Entry<String, JsonNode> entry = operationEntries.next();
                if (!HTTP_METHODS.contains(entry.getKey())) continue;

                JsonNode operation = entry.getValue();
                assertThat(operation.path("summary").asText()).isNotBlank();
                assertThat(operation.path("description").asText()).isNotBlank();
                assertThat(operation.path("responses").isObject()).isTrue();
                assertThat(operation.path("responses").isEmpty()).isFalse();
                operationCount++;
            }
        }
        assertThat(operationCount).isEqualTo(24);
    }

    private static JsonNode findParameter(JsonNode parameters, String name) {
        for (JsonNode parameter : parameters) {
            if (name.equals(parameter.path("name").asText())) return parameter;
        }
        throw new AssertionError("Parâmetro OpenAPI não encontrado: " + name);
    }
}
