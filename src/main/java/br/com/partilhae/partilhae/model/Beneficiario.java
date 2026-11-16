package br.com.partilhae.partilhae.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import br.com.partilhae.partilhae.model.converter.BooleanCharConverter;

@Entity
@Table(name = "RECEPTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idReceptor")
@Schema(
        name = "Beneficiário",
        description = "Informações complementares de um usuário com perfil de beneficiário"
)
public class Beneficiario {

    @Id
    @SequenceGenerator(name = "receptor_seq", sequenceName = "seq_receptor", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "receptor_seq")
    @Column(name = "id_receptor")
    @Schema(description = "Identificador do beneficiário no contrato atual", example = "100", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idReceptor;

    @Column(name = "qtd_pessoas_familia")
    @Schema(description = "Quantidade de pessoas na família", example = "4")
    private Integer quantidadePessoasFamilia;

    @Convert(converter = BooleanCharConverter.class)
    @Column(name = "tp_possui_criancas", columnDefinition = "CHAR(1)")
    private Boolean possuiCriancas;

    @Size(max = 200)
    @Column(name = "ds_restricao_alimentar", length = 200)
    @Schema(description = "Restrições alimentares da família", example = "Intolerância à lactose")
    private String restricaoAlimentar;

    @Size(max = 200)
    @Column(name = "ds_observacao", length = 200)
    @Schema(description = "Observações adicionais do beneficiário", example = "Família composta por quatro pessoas")
    private String observacao;

    @jakarta.validation.constraints.NotBlank
    @Size(max = 20)
    @Column(name = "tp_tipo_beneficiario", nullable = false, length = 20)
    private String tipoBeneficiario;

    @Size(max = 150)
    @Column(name = "nm_organizacao", length = 150)
    private String nomeOrganizacao;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Usuario_id_usuario", nullable = false, unique = true)
    @Schema(hidden = true)
    private Usuario usuario;
}
