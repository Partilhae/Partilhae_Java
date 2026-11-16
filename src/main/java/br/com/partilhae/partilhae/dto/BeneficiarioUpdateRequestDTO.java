package br.com.partilhae.partilhae.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "BeneficiarioUpdateRequest", description = "Campos opcionais para atualização parcial do perfil do beneficiário.")
public record BeneficiarioUpdateRequestDTO(
        @Schema(description = "Nome completo do beneficiário", example = "Maria Oliveira")
        @Size(max = 100)
        String nomeUsuario,
        
        @Schema(description = "E-mail utilizado para acesso à conta", example = "maria@partilhae.test")
        @Email
        @Size(max = 150)
        String email,
        
        @Schema(description = "Nova senha de acesso, com pelo menos 8 caracteres", accessMode = Schema.AccessMode.WRITE_ONLY)
        @Size(min = 8, max = 100)
        String senha,

        @Schema(description = "CPF com 11 dígitos", example = "12345678901")
        @Size(min = 11, max = 11)
        String cpf,
        
        @Schema(description = "Telefone de contato, com DDD", example = "11988887777")
        @Size(max = 15)
        String telefone,
        
        @Schema(description = "CEP do endereço, com 8 dígitos", example = "01310100")
        @Pattern(regexp = "^$|[0-9]{5}-?[0-9]{3}")
        String cep,
        
        @Schema(description = "Endereço residencial ou da organização", example = "Avenida Paulista")
        @Size(max = 200)
        String endereco,
        
        @Schema(description = "Número do endereço", example = "500")
        @Size(max = 20)
        String numero,
        
        @Schema(description = "Complemento do endereço", example = "Bloco A")
        @Size(max = 100)
        String complemento,
        
        @Schema(description = "Bairro", example = "Bela Vista")
        @Size(max = 80)
        String bairro,
        
        @Schema(description = "Cidade", example = "São Paulo")
        @Size(max = 80)
        String cidade,
        
        @Schema(description = "Estado ou unidade federativa", example = "SP")
        @Size(max = 80)
        String estado,
        
        @Schema(description = "Quantidade de pessoas da família", example = "4")
        @Positive
        Integer quantidadePessoasFamilia,
        
        @Schema(description = "Indica se há crianças na família", example = "true")
        Boolean possuiCriancas,
        
        @Schema(description = "Restrição alimentar informada pelo beneficiário", example = "Sem lactose")
        @Size(max = 200)
        String restricaoAlimentar,
        
        @Schema(description = "Observações adicionais do beneficiário", example = "Contato no período da tarde")
        @Size(max = 200)
        String observacao,
        
        @Schema(description = "Tipo de beneficiário", allowableValues = {"pessoaFisica", "ONG"}, example = "ONG")
        @Pattern(regexp = "pessoaFisica|ONG")
        String tipoBeneficiario,
        
        @Schema(description = "Nome da organização; obrigatório quando tipoBeneficiario for ONG", example = "Projeto Esperança")
        @Size(max = 150)
        String nomeOrganizacao

) {
    @AssertTrue(message = "Informe ao menos um campo para atualização")
    @JsonIgnore
    public boolean isAtualizacaoInformada() {
        return nomeUsuario != null || email != null || senha != null || cpf != null || telefone != null
                || cep != null || endereco != null || numero != null || complemento != null || bairro != null
                || cidade != null || estado != null || quantidadePessoasFamilia != null || possuiCriancas != null
                || restricaoAlimentar != null || observacao != null || tipoBeneficiario != null
                || nomeOrganizacao != null;
    }
}
