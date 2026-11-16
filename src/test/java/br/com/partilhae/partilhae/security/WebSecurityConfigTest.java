package br.com.partilhae.partilhae.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class WebSecurityConfigTest {
    @Test
    @DisplayName("CORS permite somente origens configuradas e não habilita credenciais")
    void origensConfiguraveisSemCredenciais() {
        CorsConfigurationSource source = new WebSecurityConfig(null, null)
                .corsConfigurationSource("https://mobile.partilhae.test, https://web.partilhae.test");
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/doadores"));
        assertThat(configuration.getAllowedOrigins()).containsExactly(
                "https://mobile.partilhae.test", "https://web.partilhae.test");
        assertThat(configuration.getAllowCredentials()).isFalse();
        assertThat(configuration.checkOrigin("https://mobile.partilhae.test")).isEqualTo("https://mobile.partilhae.test");
        assertThat(configuration.checkOrigin("https://origem-nao-configurada.test")).isNull();
    }

    @Test
    @DisplayName("Sem propriedade CORS nenhuma origem externa é liberada por padrão")
    void semOrigemPadrao() {
        CorsConfiguration configuration = new WebSecurityConfig(null, null)
                .corsConfigurationSource("")
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/doadores"));
        assertThat(configuration.getAllowedOrigins()).isEmpty();
        assertThat(configuration.getAllowedOriginPatterns()).isNullOrEmpty();
        assertThat(configuration.getAllowCredentials()).isFalse();
    }
}
