package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DenunciaResolucaoRequestDTO(@NotBlank @Size(max = 500) String resolucao) { }
