package br.com.partilhae.partilhae.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DenunciaResponse", description = "Dados de uma denúncia registrada no sistema.")
public record DenunciaResponseDTO(
        @Schema(description = "Identificador único da denúncia", example = "100") Long idDenuncia,
        @Schema(description = "Identificador do usuário denunciante", example = "25") Long idDenunciante,
        @Schema(description = "Identificador do usuário denunciado, quando esse for o alvo") Long idUsuarioDenunciado,
        @Schema(description = "Identificador da conversa denunciada, quando esse for o alvo") Long idConversa,
        @Schema(description = "Identificador da solicitação denunciada, quando esse for o alvo") Long idSolicitacao,
        @Schema(description = "Identificador da doação denunciada, quando esse for o alvo") Long idDoacao,
        @Schema(description = "Motivo informado para a denúncia", example = "A oferta não corresponde à descrição informada.") String motivo,
        @Schema(description = "Status atual da denúncia", allowableValues = {"ABERTA", "EM_ANALISE", "RESOLVIDA", "REJEITADA"}, example = "ABERTA") String status,
        @Schema(description = "Data e hora em que a denúncia foi registrada", example = "2026-01-15T12:00:00") LocalDateTime dataDenuncia,
        @Schema(description = "Texto da resolução, quando registrado", example = "Denúncia analisada e encaminhada.") String resolucao
) { }
