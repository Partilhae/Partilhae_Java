package br.com.partilhae.partilhae.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record DoadorUpdateRequestDTO(
        @Size(max = 100) String nomeUsuario,
        @Email @Size(max = 150) String email,
        @Size(min = 8, max = 100) String senha,
        @CPF String cpf,
        @Pattern(regexp = "[+0-9() .-]{10,20}") String telefone,
        @Pattern(regexp = "^$|[0-9]{5}-?[0-9]{3}") String cep,
        @Size(max = 200) String endereco,
        @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @Size(max = 80) String bairro,
        @Size(max = 80) String cidade,
        @Size(max = 80) String estado,
        @Size(max = 100) String disponibilidade,
        @Size(max = 200) String observacao,
        @Pattern(regexp = "pessoaFisica|estabelecimento|projetoSocial") String tipoDoador,
        @Size(max = 150) String nomeEstabelecimento
) {
    @AssertTrue(message = "Informe ao menos um campo para atualização")
    @JsonIgnore
    public boolean isAtualizacaoInformada() {
        return nomeUsuario != null || email != null || senha != null || cpf != null || telefone != null
                || cep != null || endereco != null || numero != null || complemento != null || bairro != null
                || cidade != null || estado != null || disponibilidade != null || observacao != null
                || tipoDoador != null || nomeEstabelecimento != null;
    }
}
