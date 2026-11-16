package br.com.partilhae.partilhae.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idUsuario")
@Schema(
        name = "Usuario",
        description = "Identidade base de um usuário do sistema (doador ou beneficiário)"
)
public class Usuario {
    @Id
    @SequenceGenerator(name = "usuario_seq", sequenceName = "seq_usuario", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @Column(name = "id_usuario")
    @Schema(description = "ID único do usuário", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idUsuario;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nm_usuario", nullable = false, length = 100)
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nomeUsuario;

    @NotBlank
    @Size(max = 150)
    @Column(name = "ds_email", nullable = false, unique = true, length = 150)
    @Schema(description = "E-mail do usuário, usado como login", example = "joao@email.com")
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(name = "ds_senha", nullable = false, length = 100)
    @Schema(description = "Hash da senha de acesso", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonIgnore
    private String senha;

    @NotBlank
    @Size(min = 11, max = 11)
    @Column(name = "num_cpf", nullable = false, unique = true, length = 11)
    @Schema(description = "CPF com 11 dígitos", example = "12345678901")
    private String cpf;

    @NotBlank
    @Size(max = 15)
    @Column(name = "num_telefone", nullable = false, length = 15)
    @Schema(description = "Telefone principal", example = "11999999999")
    private String telefone;

    @NotBlank
    @Size(max = 10)
    @Column(name = "tp_perfil", nullable = false, length = 10)
    @Schema(description = "Perfil do usuário (DOADOR ou RECEPTOR)", example = "DOADOR")
    private String perfil;

    @NotBlank
    @Size(min = 8, max = 8)
    @Column(name = "num_cep", nullable = false, length = 8)
    @Schema(description = "CEP do usuário", example = "12345678")
    private String cep;

    @NotBlank
    @Size(max = 200)
    @Column(name = "ds_endereco", nullable = false, length = 200)
    private String endereco;

    @NotBlank
    @Size(max = 20)
    @Column(name = "num_numero", length = 20, nullable = false)
    private String numero;

    @Size(max = 100)
    @Column(name = "ds_complemento", length = 100)
    private String complemento;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nm_bairro", length = 80, nullable = false)
    private String bairro;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nm_cidade", length = 80, nullable = false)
    @Schema(description = "Cidade do usuário", example = "São Paulo")
    private String cidade;

    @NotBlank
    @Size(max = 50)
    @Column(name = "nm_estado", length = 50, nullable = false)
    @Schema(description = "Estado do usuário", example = "SP")
    private String estado;

    @NotBlank
    @Size(max = 20)
    @Column(name = "tp_status_conta", nullable = false, length = 20)
    @Schema(description = "Status da conta do usuário (ATIVA ou BLOQUEADA)", example = "ATIVA")
    private String statusConta;
}
