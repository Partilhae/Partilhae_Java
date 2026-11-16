package br.com.partilhae.partilhae.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanCharConverter implements AttributeConverter<Boolean, String> {
    @Override public String convertToDatabaseColumn(Boolean value) {
        return value == null ? null : value ? "S" : "N";
    }
    @Override public Boolean convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) return null;
        if ("S".equalsIgnoreCase(value.trim())) return true;
        if ("N".equalsIgnoreCase(value.trim())) return false;
        throw new IllegalArgumentException("tp_possui_criancas deve conter S ou N");
    }
}
