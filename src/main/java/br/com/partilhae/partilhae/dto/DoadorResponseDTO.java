package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de apresentação do doador; contato e endereço são incluídos conforme a autorização do solicitante")
public record DoadorResponseDTO(
        @Schema(description = "Identificador único do doador", example = "100") Long idDoador,
        @Schema(description = "Identificador do usuário associado ao perfil", example = "25") Long idUsuario,
        @Schema(description = "Nome completo do doador ou responsável", example = "João Oliveira") String nomeUsuario,
        @Schema(description = "E-mail de contato, quando autorizado", example = "joao@partilhae.test") String email,
        @Schema(description = "Telefone de contato, quando autorizado", example = "11988887777") String telefone,
        @Schema(description = "CEP, quando autorizado", example = "01310100") String cep,
        @Schema(description = "Endereço, quando autorizado", example = "Avenida Paulista") String endereco,
        @Schema(description = "Número do endereço, quando autorizado", example = "500") String numero,
        @Schema(description = "Complemento do endereço, quando autorizado", example = "Sala 2") String complemento,
        @Schema(description = "Bairro, quando autorizado", example = "Bela Vista") String bairro,
        @Schema(description = "Cidade, quando autorizado", example = "São Paulo") String cidade,
        @Schema(description = "Estado ou unidade federativa, quando autorizado", example = "SP") String estado,
        @Schema(description = "Disponibilidade informada pelo doador", example = "Finais de semana") String disponibilidade,
        @Schema(description = "Observações sobre as doações", example = "Retirada combinada") String observacao,
        @Schema(description = "Tipo de doador", allowableValues = {"pessoaFisica", "estabelecimento", "projetoSocial"}, example = "estabelecimento") String tipoDoador,
        @Schema(description = "Nome do estabelecimento ou projeto, quando aplicável", example = "Mercado Central") String nomeEstabelecimento
) {
}
