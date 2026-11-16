package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.DoadorRequestDTO;
import br.com.partilhae.partilhae.dto.DoadorResponseDTO;
import br.com.partilhae.partilhae.dto.DoadorUpdateRequestDTO;
import br.com.partilhae.partilhae.service.DoadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doadores")
@Tag(name = "Doadores", description = "Endpoints para gerenciamento de doadores")
@RequiredArgsConstructor
public class DoadorController {

    private final DoadorService doadorService;

    @PostMapping
    @Operation(
            summary = "Cadastrar doador",
            description = "Cadastra um novo doador no sistema (signup)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doador cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado")
    })
    public ResponseEntity<DoadorResponseDTO> cadastrar(
            @RequestBody @Valid DoadorRequestDTO dto
    ) {
        DoadorResponseDTO doador = doadorService.cadastrar(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(doador);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar doadores",
            description = "Retorna uma lista paginada de doadores cadastrados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de doadores retornada com sucesso")
    })
    public ResponseEntity<Page<DoadorResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(doadorService.listar(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTOR') or hasRole('DOADOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Buscar doador por ID",
            description = "Retorna um doador específico baseado no ID informado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doador encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Doador não encontrado")
    })
    public ResponseEntity<DoadorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(doadorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar doador",
            description = "Atualiza os dados de um doador existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doador atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Doador não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<DoadorResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DoadorUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(doadorService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Deletar doador",
            description = "Remove um doador do sistema pelo ID informado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doador deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Doador não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        doadorService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
