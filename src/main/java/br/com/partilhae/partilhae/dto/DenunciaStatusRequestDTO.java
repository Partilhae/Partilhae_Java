package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DenunciaStatusRequestDTO(@NotBlank @Pattern(regexp = "ABERTA|EM_ANALISE|RESOLVIDA|REJEITADA") String status) { }
