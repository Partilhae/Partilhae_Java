package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BeneficiarioResponse", description = "Dados de apresentação do beneficiário. Informações de contato, endereço e família são retornadas conforme a autorização do solicitante.")
public record BeneficiarioResponseDTO(
        @Schema(description = "Identificador do beneficiário no contrato atual", example = "100") Long idReceptor,
        @Schema(description = "Identificador do usuário associado ao perfil", example = "25") Long idUsuario,
        @Schema(description = "Nome do beneficiário ou responsável", example = "Maria Oliveira") String nomeUsuario,
        @Schema(description = "E-mail de contato, quando autorizado", example = "maria@partilhae.test") String email,
        @Schema(description = "Telefone de contato, quando autorizado", example = "11988887777") String telefone,
        @Schema(description = "CEP, quando autorizado", example = "01310100") String cep,
        @Schema(description = "Endereço, quando autorizado", example = "Avenida Paulista") String endereco,
        @Schema(description = "Número do endereço, quando autorizado", example = "500") String numero,
        @Schema(description = "Complemento do endereço, quando autorizado", example = "Bloco A") String complemento,
        @Schema(description = "Bairro, quando autorizado", example = "Bela Vista") String bairro,
        @Schema(description = "Cidade, quando autorizado", example = "São Paulo") String cidade,
        @Schema(description = "Estado ou unidade federativa, quando autorizado", example = "SP") String estado,
        @Schema(description = "Quantidade de pessoas da família, quando autorizado", example = "4") Integer quantidadePessoasFamilia,
        @Schema(description = "Indica se há crianças na família, quando autorizado", example = "true") Boolean possuiCriancas,
        @Schema(description = "Restrição alimentar, quando autorizada", example = "Sem lactose") String restricaoAlimentar,
        @Schema(description = "Observações adicionais, quando autorizadas", example = "Contato no período da tarde") String observacao,
        @Schema(description = "Tipo de beneficiário", allowableValues = {"pessoaFisica", "ONG"}, example = "ONG") String tipoBeneficiario,
        @Schema(description = "Nome da organização, quando aplicável", example = "Projeto Esperança") String nomeOrganizacao
) {
}
