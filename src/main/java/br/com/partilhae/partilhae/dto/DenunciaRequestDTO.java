package br.com.partilhae.partilhae.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(name = "DenunciaRequest", description = "Dados para registrar uma denúncia. Informe exatamente um dos identificadores de alvo.")
public record DenunciaRequestDTO(
        @Schema(description = "Identificador do usuário denunciado; use somente um identificador de alvo", example = "50")
        @Positive
        Long idUsuarioDenunciado,
        
        @Schema(description = "Identificador da conversa denunciada; use somente um identificador de alvo")
        @Positive
        Long idConversa,
        
        @Schema(description = "Identificador da solicitação denunciada; use somente um identificador de alvo")
        @Positive
        Long idSolicitacao,
        
        @Schema(description = "Identificador da doação denunciada; use somente um identificador de alvo")
        @Positive
        Long idDoacao,
        
        @Schema(description = "Descrição do motivo da denúncia", example = "A oferta não corresponde à descrição informada.")
        @NotBlank@Size(max = 500)
        String motivo) {

    @AssertTrue(message = "Informe exatamente um alvo para a denúncia")
    @JsonIgnore
    public boolean isAlvoUnico() {
        int count = (idUsuarioDenunciado == null ? 0 : 1) + (idConversa == null ? 0 : 1)
                + (idSolicitacao == null ? 0 : 1) + (idDoacao == null ? 0 : 1);
        return count == 1;
    }
}
