package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.BeneficiarioRequestDTO;
import br.com.partilhae.partilhae.dto.BeneficiarioResponseDTO;
import br.com.partilhae.partilhae.dto.BeneficiarioUpdateRequestDTO;
import br.com.partilhae.partilhae.service.BeneficiarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/beneficiarios")
@Tag(name = "Beneficiários", description = "Endpoints para cadastro, consulta e gerenciamento de beneficiários.")
@RequiredArgsConstructor
public class BeneficiarioController {
    private final BeneficiarioService beneficiarioService;

    @PostMapping
    @Operation(
            summary = "Cadastrar beneficiário",
            description = "Cadastra um beneficiário e seu perfil com os dados informados. O endpoint é público e aceita o formato de requisição utilizado pelo aplicativo Mobile."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Beneficiário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<BeneficiarioResponseDTO> cadastrar(@Valid @RequestBody BeneficiarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiarioService.cadastrar(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar beneficiários",
            description = "Retorna uma página de beneficiários cadastrados. A consulta aceita os parâmetros de paginação page, size e sort e é restrita a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de beneficiários retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<BeneficiarioResponseDTO>> listar(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(beneficiarioService.listar(pageable));
    }

    @GetMapping("/criancas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Filtrar beneficiários com crianças",
            description = "Retorna uma página de beneficiários filtrada pela presença de crianças na família. O parâmetro possuiCriancas aceita S ou N, sem distinção entre maiúsculas e minúsculas; a consulta é restrita a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Beneficiários filtrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro possuiCriancas inválido"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<BeneficiarioResponseDTO>> buscarPorPossuiCriancas(
            @Parameter(description = "Filtro de presença de crianças: S para sim ou N para não", required = true, example = "S")
            @RequestParam @Pattern(regexp = "[SNsn]") String possuiCriancas,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(beneficiarioService.buscarPorPossuiCriancas(possuiCriancas, pageable));
    }

    @GetMapping("/restricao-alimentar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Filtrar beneficiários por restrição alimentar",
            description = "Retorna uma página de beneficiários cuja restrição alimentar contém o termo informado, sem distinção entre maiúsculas e minúsculas. A consulta é restrita a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Beneficiários filtrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro termo inválido"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<BeneficiarioResponseDTO>> buscarPorRestricaoAlimentar(
            @Parameter(description = "Termo procurado na restrição alimentar", required = true, example = "lactose")
            @RequestParam @NotBlank String termo,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(beneficiarioService.buscarPorRestricaoAlimentar(termo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR') or hasRole('RECEPTOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Buscar beneficiário por ID",
            description = "Retorna os dados de um beneficiário cadastrado a partir do identificador informado. Informações privadas são incluídas conforme as regras de acesso do solicitante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Beneficiário encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BeneficiarioResponseDTO> buscarPorId(
            @Parameter(description = "Identificador do beneficiário", required = true, example = "100")
            @PathVariable Long id) {
        return ResponseEntity.ok(beneficiarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar beneficiário",
            description = "Atualiza parcialmente os dados pessoais e o perfil do beneficiário identificado. Informe ao menos um campo no corpo da requisição."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Beneficiário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BeneficiarioResponseDTO> atualizar(
            @Parameter(description = "Identificador do beneficiário", required = true, example = "100")
            @PathVariable Long id,
            @RequestBody @Valid BeneficiarioUpdateRequestDTO dto) {
        return ResponseEntity.ok(beneficiarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Excluir beneficiário",
            description = "Desativa a conta do beneficiário identificado, bloqueando seu usuário sem excluir fisicamente os registros."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta do beneficiário desativada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> desativar(
            @Parameter(description = "Identificador do beneficiário", required = true, example = "100")
            @PathVariable Long id) {
        beneficiarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
