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

@Schema(name = "BeneficiarioRequest", description = "Dados para cadastrar um beneficiário e seu perfil no Partilhaê.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record BeneficiarioRequestDTO(
        @Schema(description = "Nome completo do beneficiário", example = "Maria Oliveira")
        @NotBlank @Size(max = 100) String nomeUsuario,
        @Schema(description = "E-mail utilizado para acesso à conta", example = "maria@partilhae.test")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(description = "Senha de acesso, com pelo menos 8 caracteres", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 100) String senha,
        @Schema(description = "CPF válido do responsável pelo cadastro, com 11 dígitos após a normalização")
        @NotBlank @CPF String cpf,
        @Schema(description = "Telefone de contato, com DDD", example = "11988887777")
        @NotBlank @Pattern(regexp = "[+0-9() .-]{10,20}") String telefone,
        @Schema(description = "CEP do endereço, com 8 dígitos", example = "01310100")
        @NotBlank @Pattern(regexp = "[0-9]{5}-?[0-9]{3}") String cep,
        @Schema(description = "Endereço residencial ou da organização", example = "Avenida Paulista")
        @NotBlank @Size(max = 200) String endereco,
        @Schema(description = "Número do endereço", example = "500")
        @NotBlank @Size(max = 20) String numero,
        @Schema(description = "Complemento do endereço", example = "Bloco A")
        @Size(max = 100) String complemento,
        @Schema(description = "Bairro", example = "Bela Vista")
        @NotBlank @Size(max = 80) String bairro,
        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank @Size(max = 80) String cidade,
        @Schema(description = "Estado ou unidade federativa", example = "SP")
        @NotBlank @Size(max = 80) String estado,
        @Schema(description = "Quantidade de pessoas da família", example = "4")
        @jakarta.validation.constraints.Positive Integer quantidadePessoasFamilia,
        @Schema(description = "Indica se há crianças na família", example = "true")
        Boolean possuiCriancas,
        @Schema(description = "Restrição alimentar informada pelo beneficiário", example = "Sem lactose")
        @Size(max = 200) String restricaoAlimentar,
        @Schema(description = "Observações adicionais do beneficiário", example = "Contato no período da tarde")
        @Size(max = 200) String observacao,
        @Schema(description = "Tipo de beneficiário cadastrado", allowableValues = {"pessoaFisica", "ONG"}, example = "ONG")
        @NotBlank @Pattern(regexp = "pessoaFisica|ONG") String tipoBeneficiario,
        @Schema(description = "Nome da organização; obrigatório quando tipoBeneficiario for ONG", example = "Projeto Esperança")
        @Size(max = 150) String nomeOrganizacao
) {
    @AssertTrue(message = "nomeOrganizacao é obrigatório para ONG")
    @JsonIgnore
    public boolean isNomeOrganizacaoValido() {
        if ("pessoaFisica".equals(tipoBeneficiario)) return nomeOrganizacao == null || nomeOrganizacao.isBlank();
        return "ONG".equals(tipoBeneficiario) ? nomeOrganizacao != null && !nomeOrganizacao.isBlank() : false;
    }
}
