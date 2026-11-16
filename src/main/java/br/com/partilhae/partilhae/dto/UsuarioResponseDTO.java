package br.com.partilhae.partilhae.dto;

public record UsuarioResponseDTO(Long idUsuario, String nomeUsuario, String email, String cpf,
                                 String telefone, String cep, String endereco, String numero,
                                 String complemento, String bairro, String cidade, String estado,
                                 String statusConta) { }
