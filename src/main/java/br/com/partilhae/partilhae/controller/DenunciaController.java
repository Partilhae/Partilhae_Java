package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.DenunciaRequestDTO;
import br.com.partilhae.partilhae.dto.DenunciaResponseDTO;
import br.com.partilhae.partilhae.dto.DenunciaResolucaoRequestDTO;
import br.com.partilhae.partilhae.dto.DenunciaStatusRequestDTO;
import br.com.partilhae.partilhae.service.DenunciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/denuncias")
@Tag(name = "Denúncias", description = "Endpoints para registro, consulta e gerenciamento de denúncias.")
@RequiredArgsConstructor
public class DenunciaController {
    private final DenunciaService denunciaService;

    @PostMapping
    @Operation(
            summary = "Registrar denúncia",
            description = "Registra uma denúncia associada a exatamente um alvo: usuário, conversa, solicitação ou doação. O usuário denunciante é obtido do token de autenticação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Denúncia registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário denunciado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DenunciaResponseDTO> criar(@Valid @RequestBody DenunciaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(denunciaService.cadastrar(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar denúncias",
            description = "Retorna uma página com as denúncias cadastradas. A consulta aceita os parâmetros de paginação page, size e sort e é restrita a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de denúncias retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<DenunciaResponseDTO>> listar(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(denunciaService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar denúncia por ID",
            description = "Retorna os dados de uma denúncia a partir do identificador informado. A consulta é permitida ao denunciante da ocorrência ou a um administrador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Denúncia encontrada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Denúncia não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DenunciaResponseDTO> buscar(
            @Parameter(description = "Identificador da denúncia", required = true, example = "100")
            @PathVariable Long id) {
        return ResponseEntity.ok(denunciaService.buscar(id));
    }

    @GetMapping("/usuario/{id}")
    @Operation(
            summary = "Listar denúncias de um usuário",
            description = "Retorna uma página das denúncias registradas pelo usuário identificado. O acesso é permitido ao próprio usuário ou a um administrador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Denúncias do usuário retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<DenunciaResponseDTO>> listarPorUsuario(
            @Parameter(description = "Identificador do usuário denunciante", required = true, example = "100")
            @PathVariable Long id,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(denunciaService.listarPorUsuario(id, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar status da denúncia",
            description = "Altera o status da denúncia para ABERTA, EM_ANALISE, RESOLVIDA ou REJEITADA. Operação restrita a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status da denúncia atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Denúncia não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DenunciaResponseDTO> status(
            @Parameter(description = "Identificador da denúncia", required = true, example = "100")
            @PathVariable Long id,
            @Valid @RequestBody DenunciaStatusRequestDTO dto) {
        return ResponseEntity.ok(denunciaService.atualizarStatus(id, dto.status()));
    }

    @PatchMapping("/{id}/resolucao")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Registrar resolução da denúncia",
            description = "Registra o texto de resolução para a denúncia e altera seu status para RESOLVIDA. Operação restrita a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resolução da denúncia registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Denúncia não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DenunciaResponseDTO> resolucao(
            @Parameter(description = "Identificador da denúncia", required = true, example = "100")
            @PathVariable Long id,
            @Valid @RequestBody DenunciaResolucaoRequestDTO dto) {
        return ResponseEntity.ok(denunciaService.atualizarResolucao(id, dto.resolucao()));
    }
}
