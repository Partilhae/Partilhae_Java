package br.com.partilhae.partilhae.dto;

import jakarta.validation.constraints.Email;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

@Schema(name = "UsuarioUpdateRequest", description = "Campos opcionais para atualização parcial dos dados do usuário.")
public record UsuarioRequestDTO(
        @Schema(description = "Nome completo do usuário", example = "Maria Oliveira")
        @Size(max = 100) String nomeUsuario,
        @Schema(description = "E-mail utilizado para acesso à conta", example = "maria@partilhae.test")
        @Email @Size(max = 150) String email,
        @Schema(description = "Nova senha de acesso, com pelo menos 8 caracteres", accessMode = Schema.AccessMode.WRITE_ONLY)
        @Size(min = 8, max = 100) String senha,
        @Schema(description = "CPF válido do usuário, com 11 dígitos após a normalização")
        @CPF String cpf,
        @Schema(description = "Telefone de contato, com DDD", example = "11988887777")
        @Pattern(regexp = "[+0-9() .-]{10,20}") String telefone,
        @Schema(description = "CEP do endereço, com 8 dígitos", example = "01310100")
        @Pattern(regexp = "^$|[0-9]{5}-?[0-9]{3}") String cep,
        @Schema(description = "Endereço do usuário", example = "Avenida Paulista")
        @Size(max = 200) String endereco,
        @Schema(description = "Número do endereço", example = "500")
        @Size(max = 20) String numero,
        @Schema(description = "Complemento do endereço", example = "Bloco A")
        @Size(max = 100) String complemento,
        @Schema(description = "Bairro", example = "Bela Vista")
        @Size(max = 80) String bairro,
        @Schema(description = "Cidade", example = "São Paulo")
        @Size(max = 80) String cidade,
        @Schema(description = "Estado ou unidade federativa", example = "SP")
        @Size(max = 80) String estado
) {
    @AssertTrue(message = "Informe ao menos um campo para atualização")
    @JsonIgnore
    public boolean isAtualizacaoInformada() {
        return nomeUsuario != null || email != null || senha != null || cpf != null || telefone != null
                || cep != null || endereco != null || numero != null || complemento != null
                || bairro != null || cidade != null || estado != null;
    }
}
