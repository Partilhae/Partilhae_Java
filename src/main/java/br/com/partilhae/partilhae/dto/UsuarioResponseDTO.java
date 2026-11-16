package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UsuarioResponse", description = "Dados de identificação, contato e endereço do usuário.")
public record UsuarioResponseDTO(
        @Schema(description = "Identificador único do usuário", example = "100") Long idUsuario,
        @Schema(description = "Nome completo do usuário", example = "Maria Oliveira") String nomeUsuario,
        @Schema(description = "E-mail utilizado para acesso à conta", example = "maria@partilhae.test") String email,
        @Schema(description = "CPF do usuário, quando retornado pelo serviço") String cpf,
        @Schema(description = "Telefone de contato", example = "11988887777") String telefone,
        @Schema(description = "CEP do endereço, com 8 dígitos", example = "01310100") String cep,
        @Schema(description = "Endereço do usuário", example = "Avenida Paulista") String endereco,
        @Schema(description = "Número do endereço", example = "500") String numero,
        @Schema(description = "Complemento do endereço", example = "Bloco A") String complemento,
        @Schema(description = "Bairro", example = "Bela Vista") String bairro,
        @Schema(description = "Cidade", example = "São Paulo") String cidade,
        @Schema(description = "Estado ou unidade federativa", example = "SP") String estado,
        @Schema(description = "Status atual da conta", allowableValues = {"ATIVA", "BLOQUEADA"}, example = "ATIVA") String statusConta
) { }
