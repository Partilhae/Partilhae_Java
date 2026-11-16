package br.com.partilhae.partilhae.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger: título e descrição exibidos no topo da página
 * e o esquema Bearer, que faz aparecer o botão "Authorize" para usar o
 * token JWT nas rotas protegidas.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    private static final String TITULO = "Partilhaê API";
    private static final String DESCRICAO =
            "API REST do Partilhaê, plataforma voltada à conexão entre doadores de alimentos e beneficiários, " +
            "com funcionalidades de cadastro, autenticação e gerenciamento de denúncias.";
    private static final String VERSAO = "1.0.0";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITULO)
                        .description(DESCRICAO)
                        .version(VERSAO))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
