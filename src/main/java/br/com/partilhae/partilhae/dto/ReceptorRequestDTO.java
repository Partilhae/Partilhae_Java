package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

@Schema(description = "Dados para cadastro ou atualização de um receptor")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReceptorRequestDTO(
        @NotBlank @Size(max = 100) String nomeUsuario,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String senha,
        @NotBlank @CPF String cpf,
        @NotBlank @Pattern(regexp = "[+0-9() .-]{10,20}") String telefone,
        @NotBlank @Pattern(regexp = "[0-9]{5}-?[0-9]{3}") String cep,
        @NotBlank @Size(max = 200) String endereco,
        @NotBlank @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @NotBlank @Size(max = 80) String bairro,
        @NotBlank @Size(max = 80) String cidade,
        @NotBlank @Size(max = 80) String estado,
        @jakarta.validation.constraints.Positive Integer quantidadePessoasFamilia,
        Boolean possuiCriancas,
        @Size(max = 200) String restricaoAlimentar,
        @Size(max = 200) String observacao,
        @NotBlank @Pattern(regexp = "pessoaFisica|ONG") String tipoBeneficiario,
        @Size(max = 150) String nomeOrganizacao
) {
    @AssertTrue(message = "nomeOrganizacao é obrigatório para ONG")
    @JsonIgnore
    public boolean isNomeOrganizacaoValido() {
        if ("pessoaFisica".equals(tipoBeneficiario)) return nomeOrganizacao == null || nomeOrganizacao.isBlank();
        return "ONG".equals(tipoBeneficiario) ? nomeOrganizacao != null && !nomeOrganizacao.isBlank() : false;
    }
}
