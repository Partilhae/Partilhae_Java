package br.com.partilhae.partilhae.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Resposta de login: token JWT + dados básicos do usuário autenticado")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record JwtResponseDTO(

        @Schema(description = "Token JWT a ser enviado no header Authorization: Bearer <token>")
        String token,

        @Schema(example = "Bearer")
        String tipoToken,

        @Schema(example = "1")
        Long idUsuario,

        @Schema(example = "João Silva")
        String nomeUsuario,

        @Schema(example = "joao@email.com")
        String email,

        @Schema(description = "Papel do usuário no sistema", example = "DOADOR")
        String role,

        @Schema(description = "Preenchido apenas se o usuário for um Doador", example = "1")
        Long idDoador,

        @Schema(description = "Preenchido apenas se o usuário for um Receptor", example = "null")
        Long idReceptor

) {
}
