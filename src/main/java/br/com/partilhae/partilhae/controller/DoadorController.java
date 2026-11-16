package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.DoadorRequestDTO;
import br.com.partilhae.partilhae.dto.DoadorResponseDTO;
import br.com.partilhae.partilhae.dto.DoadorUpdateRequestDTO;
import br.com.partilhae.partilhae.service.DoadorService;
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
@RequestMapping("/doadores")
@Tag(name = "Doadores", description = "Endpoints para cadastro, consulta e gerenciamento de doadores.")
@RequiredArgsConstructor
public class DoadorController {

    private final DoadorService doadorService;

    @PostMapping
    @Operation(
            summary = "Cadastrar doador",
            description = "Cadastra um doador e seu perfil a partir dos dados informados. O endpoint é público e retorna os dados do perfil criado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doador cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<DoadorResponseDTO> cadastrar(@RequestBody @Valid DoadorRequestDTO dto) {
        DoadorResponseDTO doador = doadorService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(doador);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar doadores",
            description = "Retorna uma página de doadores cadastrados. A consulta aceita os parâmetros de paginação page, size e sort e é restrita a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de doadores retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<DoadorResponseDTO>> listar(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(doadorService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTOR') or hasRole('DOADOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Buscar doador por ID",
            description = "Retorna os dados de um doador cadastrado a partir do identificador informado. A inclusão de contato e endereço segue as regras de autorização do solicitante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doador encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Doador não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DoadorResponseDTO> buscarPorId(
            @Parameter(description = "Identificador do doador", required = true, example = "100")
            @PathVariable Long id) {
        return ResponseEntity.ok(doadorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar doador",
            description = "Atualiza parcialmente os dados pessoais e o perfil do doador identificado. Informe ao menos um campo no corpo da requisição."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doador atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Doador não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DoadorResponseDTO> atualizar(
            @Parameter(description = "Identificador do doador", required = true, example = "100")
            @PathVariable Long id,
            @RequestBody @Valid DoadorUpdateRequestDTO dto) {
        return ResponseEntity.ok(doadorService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Excluir doador",
            description = "Desativa a conta do doador identificado, bloqueando seu usuário sem excluir fisicamente os registros."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta do doador desativada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Doador não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do doador", required = true, example = "100")
            @PathVariable Long id) {
        doadorService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
