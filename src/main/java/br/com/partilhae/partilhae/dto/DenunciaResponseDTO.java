package br.com.partilhae.partilhae.dto;

import java.time.LocalDateTime;

public record DenunciaResponseDTO(Long idDenuncia, Long idDenunciante, Long idUsuarioDenunciado,
                                  Long idConversa, Long idSolicitacao, Long idDoacao,
                                  String motivo, String status, LocalDateTime dataDenuncia, String resolucao) { }
