package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DenunciaResolucaoRequest", description = "Texto que descreve a resolução da denúncia.")
public record DenunciaResolucaoRequestDTO(
        @Schema(description = "Descrição da providência ou conclusão da análise", example = "Denúncia analisada e encaminhada.")
        @NotBlank
        @Size(max = 500)
        String resolucao
) { }
