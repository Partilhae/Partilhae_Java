package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.ReceptorResponseDTO;
import br.com.partilhae.partilhae.dto.ReceptorUpdateRequestDTO;
import br.com.partilhae.partilhae.service.ReceptorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/receptores")
@RequiredArgsConstructor
public class ReceptorController {
    private final ReceptorService receptorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReceptorResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(receptorService.listar(pageable));
    }

    @GetMapping("/criancas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReceptorResponseDTO>> buscarPorPossuiCriancas(
            @RequestParam @Pattern(regexp = "[SNsn]") String possuiCriancas, Pageable pageable) {
        return ResponseEntity.ok(receptorService.buscarPorPossuiCriancas(possuiCriancas, pageable));
    }

    @GetMapping("/restricao-alimentar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReceptorResponseDTO>> buscarPorRestricaoAlimentar(
            @RequestParam @NotBlank String termo, Pageable pageable) {
        return ResponseEntity.ok(receptorService.buscarPorRestricaoAlimentar(termo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOADOR') or hasRole('RECEPTOR') or hasRole('ADMIN')")
    public ResponseEntity<ReceptorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(receptorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTOR') or hasRole('ADMIN')")
    public ResponseEntity<ReceptorResponseDTO> atualizar(@PathVariable Long id,
                                                          @RequestBody @Valid ReceptorUpdateRequestDTO dto) {
        return ResponseEntity.ok(receptorService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        receptorService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
