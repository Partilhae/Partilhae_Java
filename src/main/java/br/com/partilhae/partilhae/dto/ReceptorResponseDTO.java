package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de apresentação do receptor; contato, endereço e dados familiares são incluídos conforme a autorização do solicitante")
public record ReceptorResponseDTO(
        Long idReceptor,
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
        Integer quantidadePessoasFamilia,
        Boolean possuiCriancas,
        String restricaoAlimentar,
        String observacao,
        String tipoBeneficiario,
        String nomeOrganizacao
) {
}
