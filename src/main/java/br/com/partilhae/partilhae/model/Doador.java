package br.com.partilhae.partilhae.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "DOADOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idDoador")
@Schema(
        name = "Doador",
        description = "Informações complementares de um usuário com perfil de doador"
)
public class Doador {
    @Id
    @SequenceGenerator(name = "doador_seq", sequenceName = "seq_doador", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doador_seq")
    @Column(name = "id_doador")
    @Schema(description = "ID único do doador", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idDoador;

    @Size(max = 100)
    @Column(name = "ds_disponibilidade", length = 100)
    @Schema(description = "Disponibilidade do doador para realizar doações", example = "Disponível durante os finais de semana")
    private String disponibilidade;

    @Size(max = 200)
    @Column(name = "ds_observacao", length = 200)
    @Schema(description = "Observações adicionais do doador", example = "Posso realizar entregas na região central")
    private String observacao;

    @jakarta.validation.constraints.NotBlank
    @Size(max = 20)
    @Column(name = "tp_tipo_doador", nullable = false, length = 20)
    private String tipoDoador;

    @Size(max = 150)
    @Column(name = "nm_estabelecimento", length = 150)
    private String nomeEstabelecimento;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Usuario_id_usuario", nullable = false, unique = true)
    @Schema(hidden = true)
    private Usuario usuario;
}
