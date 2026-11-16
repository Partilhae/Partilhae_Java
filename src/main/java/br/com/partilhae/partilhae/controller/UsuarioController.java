package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.StatusContaRequestDTO;
import br.com.partilhae.partilhae.dto.UsuarioRequestDTO;
import br.com.partilhae.partilhae.dto.UsuarioResponseDTO;
import br.com.partilhae.partilhae.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Endpoints para consulta, atualização e gerenciamento de usuários.")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários", description = "Retorna uma página de usuários cadastrados. A consulta aceita os parâmetros de paginação page, size e sort e é restrita a administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<UsuarioResponseDTO>> listar(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados de um usuário cadastrado a partir do identificador informado, conforme as regras de acesso da conta autenticada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UsuarioResponseDTO> buscar(
            @Parameter(description = "Identificador do usuário", required = true, example = "100")
            @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza parcialmente os dados pessoais e de endereço do usuário identificado. Informe ao menos um campo no corpo da requisição.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @Parameter(description = "Identificador do usuário", required = true, example = "100")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário", description = "Bloqueia a conta do usuário identificado, sem remover fisicamente o registro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta do usuário bloqueada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do usuário", required = true, example = "100")
            @PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar status da conta", description = "Altera o status da conta do usuário para um dos valores aceitos pela API: ATIVA ou BLOQUEADA. Operação restrita a administradores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status da conta atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UsuarioResponseDTO> atualizarStatus(
            @Parameter(description = "Identificador do usuário", required = true, example = "100")
            @PathVariable Long id,
            @Valid @RequestBody StatusContaRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizarStatus(id, dto.statusConta()));
    }
}
