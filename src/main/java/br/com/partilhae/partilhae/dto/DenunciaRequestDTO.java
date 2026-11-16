package br.com.partilhae.partilhae.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DenunciaRequestDTO(
        @Positive Long idUsuarioDenunciado,
        @Positive Long idConversa,
        @Positive Long idSolicitacao,
        @Positive Long idDoacao,
        @NotBlank @Size(max = 500) String motivo) {

    @AssertTrue(message = "Informe exatamente um alvo para a denúncia")
    @JsonIgnore
    public boolean isAlvoUnico() {
        int count = (idUsuarioDenunciado == null ? 0 : 1) + (idConversa == null ? 0 : 1)
                + (idSolicitacao == null ? 0 : 1) + (idDoacao == null ? 0 : 1);
        return count == 1;
    }
}
