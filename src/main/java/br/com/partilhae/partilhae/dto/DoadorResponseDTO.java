package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de apresentação do doador; contato e endereço são incluídos conforme a autorização do solicitante")
public record DoadorResponseDTO(
        Long idDoador,
        Long idUsuario,
        String nomeUsuario,
        String email,
        String telefone,
        String cep,
        String endereco,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String disponibilidade,
        String observacao,
        String tipoDoador,
        String nomeEstabelecimento
) {
}
