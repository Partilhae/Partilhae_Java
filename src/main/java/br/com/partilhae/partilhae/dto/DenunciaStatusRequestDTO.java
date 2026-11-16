package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DenunciaStatusRequest", description = "Status a ser atribuído à denúncia.")
public record DenunciaStatusRequestDTO(
        @Schema(description = "Novo status da denúncia", allowableValues = {"ABERTA", "EM_ANALISE", "RESOLVIDA", "REJEITADA"}, example = "EM_ANALISE")
        @NotBlank @Pattern(regexp = "ABERTA|EM_ANALISE|RESOLVIDA|REJEITADA") String status
) { }
