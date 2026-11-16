package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.ReceptorRequestDTO;
import br.com.partilhae.partilhae.dto.ReceptorResponseDTO;
import br.com.partilhae.partilhae.service.ReceptorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BeneficiarioCompatibilityController {
    private final ReceptorService receptorService;

    @PostMapping("/beneficiarios")
    public ResponseEntity<ReceptorResponseDTO> cadastrar(@Valid @RequestBody ReceptorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receptorService.cadastrar(dto));
    }
}
