package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "Credenciais usadas para autenticar um usuário.")
public record LoginRequestDTO(
        @Schema(description = "E-mail cadastrado na conta", example = "maria@partilhae.test")
        @NotBlank
        @Email
        String email,
        
        @Schema(description = "Senha de acesso", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        String senha
) {
}
