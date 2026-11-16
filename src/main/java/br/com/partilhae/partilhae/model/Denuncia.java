package br.com.partilhae.partilhae.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "DENUNCIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idDenuncia")
public class Denuncia {
    @Id
    @SequenceGenerator(
            name = "denuncia_seq",
            sequenceName = "seq_denuncia",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "denuncia_seq"
    )
    @Column(name = "id_denuncia")
    private Long idDenuncia;

    @NotBlank
    @Size(max = 500)
    @Column(name = "ds_motivo", nullable = false, length = 500)
    private String motivo;

    @NotBlank
    @Size(max = 20)
    @Column(name = "tp_status", nullable = false, length = 20)
    private String status;

    @Column(name = "dt_denuncia", nullable = false, columnDefinition = "DATE")
    private LocalDateTime dataDenuncia;

    @Size(max = 500)
    @Column(name = "ds_resolucao", length = 500)
    private String resolucao;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Usuario_id_denunciante", nullable = false)
    @JsonIgnore
    private Usuario denunciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_id_denunciado")
    @JsonIgnore
    private Usuario denunciado;

    @Column(name = "Doacao_id_doacao")
    private Long idDoacao;

    @Column(name = "Conversa_id_conversa")
    private Long idConversa;

    @Column(name = "Solicitacao_id_solicitacao")
    private Long idSolicitacao;

    @PrePersist
    protected void aoCriar() {
        if (dataDenuncia == null) {
            dataDenuncia = LocalDateTime.now();
        }

        if (status == null) {
            status = "ABERTA";
        }
        validarAlvoUnico();
    }

    @PreUpdate
    protected void validarAlvoUnico() {
        int targets = (denunciado == null ? 0 : 1) + (idDoacao == null ? 0 : 1)
                + (idConversa == null ? 0 : 1) + (idSolicitacao == null ? 0 : 1);
        if (targets != 1) throw new IllegalStateException("A denúncia deve possuir exatamente um alvo");
    }
}
