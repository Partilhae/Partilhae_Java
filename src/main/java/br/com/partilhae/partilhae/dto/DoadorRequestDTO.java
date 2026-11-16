package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "DoadorRequest", description = "Dados para cadastrar um doador e seu perfil no Partilhaê.")
public record DoadorRequestDTO(
        @Schema(description = "Nome completo do doador ou responsável", example = "João Oliveira")
        @NotBlank @Size(max = 100)
        String nomeUsuario,
        
        @Schema(description = "E-mail utilizado para acesso à conta", example = "joao@partilhae.test")
        @NotBlank
        @Email
        @Size(max = 150)
        String email,
        
        @Schema(description = "Senha de acesso, com pelo menos 8 caracteres", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        @Size(min = 8, max = 100)
        String senha,

        @Schema(description = "CPF com 11 dígitos", example = "12345678901")
        @NotBlank
        @Size(min = 11, max = 11)
        String cpf,
        
        @Schema(description = "Telefone de contato, com DDD", example = "11988887777")
        @NotBlank
        @Size(max = 15)
        String telefone,
        
        @Schema(description = "CEP do endereço, com 8 dígitos", example = "01310100")
        @NotBlank
        @Pattern(regexp = "[0-9]{5}-?[0-9]{3}")
        String cep,
        
        @Schema(description = "Endereço do doador", example = "Avenida Paulista")
        @NotBlank
        @Size(max = 200)
        String endereco,
        
        @Schema(description = "Número do endereço", example = "500")
        @NotBlank
        @Size(max = 20)
        String numero,
        
        @Schema(description = "Complemento do endereço", example = "Sala 2")
        @Size(max = 100)
        String complemento,
        
        @Schema(description = "Bairro", example = "Bela Vista")
        @NotBlank
        @Size(max = 80)
        String bairro,
        
        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank
        @Size(max = 80)
        String cidade,
        
        @Schema(description = "Estado ou unidade federativa", example = "SP")
        @NotBlank
        @Size(max = 80)
        String estado,
        
        @Schema(description = "Disponibilidade informada pelo doador", example = "Finais de semana")
        @Size(max = 100)
        String disponibilidade,
        
        @Schema(description = "Observações adicionais sobre as doações", example = "Retirada combinada")
        @Size(max = 200)
        String observacao,
        
        @Schema(description = "Tipo de doador", allowableValues = {"pessoaFisica", "estabelecimento", "projetoSocial"}, example = "estabelecimento")
        @NotBlank
        @Pattern(regexp = "pessoaFisica|estabelecimento|projetoSocial")
        String tipoDoador,
        
        @Schema(description = "Nome do estabelecimento ou projeto; obrigatório para estabelecimento e projetoSocial", example = "Mercado Central")
        @Size(max = 150)
        String nomeEstabelecimento
) {
    @AssertTrue(message = "nomeEstabelecimento é obrigatório para estabelecimento ou projetoSocial")
    @JsonIgnore
    public boolean isNomeEstabelecimentoValido() {
        if ("pessoaFisica".equals(tipoDoador)) return nomeEstabelecimento == null || nomeEstabelecimento.isBlank();
        return "estabelecimento".equals(tipoDoador) || "projetoSocial".equals(tipoDoador)
                ? nomeEstabelecimento != null && !nomeEstabelecimento.isBlank() : false;
    }
}
