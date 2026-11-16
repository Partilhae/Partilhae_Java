package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StatusContaRequestDTO(@NotBlank @Pattern(regexp = "ATIVA|BLOQUEADA") String statusConta) { }
