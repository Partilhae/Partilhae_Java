package br.com.partilhae.partilhae.model.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BooleanCharConverterTest {
    private final BooleanCharConverter converter = new BooleanCharConverter();

    @Test
    @DisplayName("Converte valores booleanos para S e N")
    void converterParaBanco() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("S");
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo("N");
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("Converte S e N sem diferenciar maiúsculas e espaços externos")
    void converterParaEntidade() {
        assertThat(converter.convertToEntityAttribute("S")).isTrue();
        assertThat(converter.convertToEntityAttribute("N")).isFalse();
        assertThat(converter.convertToEntityAttribute("s")).isTrue();
        assertThat(converter.convertToEntityAttribute(" n ")).isFalse();
        assertThat(converter.convertToEntityAttribute("")).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("Rejeita valor de banco diferente de S ou N")
    void valorInvalido() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("X"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
