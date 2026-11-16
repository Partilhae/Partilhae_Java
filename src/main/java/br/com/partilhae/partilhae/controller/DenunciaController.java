package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.DenunciaRequestDTO;
import br.com.partilhae.partilhae.dto.DenunciaResponseDTO;
import br.com.partilhae.partilhae.dto.DenunciaResolucaoRequestDTO;
import br.com.partilhae.partilhae.dto.DenunciaStatusRequestDTO;
import br.com.partilhae.partilhae.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {
    private final DenunciaService denunciaService;

    @PostMapping
    public ResponseEntity<DenunciaResponseDTO> criar(@Valid @RequestBody DenunciaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(denunciaService.cadastrar(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DenunciaResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(denunciaService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DenunciaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(denunciaService.buscar(id));
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<Page<DenunciaResponseDTO>> listarPorUsuario(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(denunciaService.listarPorUsuario(id, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DenunciaResponseDTO> status(@PathVariable Long id,
                                                       @Valid @RequestBody DenunciaStatusRequestDTO dto) {
        return ResponseEntity.ok(denunciaService.atualizarStatus(id, dto.status()));
    }

    @PatchMapping("/{id}/resolucao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DenunciaResponseDTO> resolucao(@PathVariable Long id,
                                                          @Valid @RequestBody DenunciaResolucaoRequestDTO dto) {
        return ResponseEntity.ok(denunciaService.atualizarResolucao(id, dto.resolucao()));
    }
}
