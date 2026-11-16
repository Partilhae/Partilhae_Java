package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "StatusContaRequest", description = "Novo status da conta do usuário.")
public record StatusContaRequestDTO(
        @Schema(description = "Status a ser definido para a conta", allowableValues = {"ATIVA", "BLOQUEADA"}, example = "ATIVA")
        @NotBlank
        @Pattern(regexp = "ATIVA|BLOQUEADA")
        String statusConta
) {
}
