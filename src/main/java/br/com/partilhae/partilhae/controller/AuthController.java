package br.com.partilhae.partilhae.controller;

import br.com.partilhae.partilhae.dto.JwtResponseDTO;
import br.com.partilhae.partilhae.dto.LoginRequestDTO;
import br.com.partilhae.partilhae.security.JwtUtils;
import br.com.partilhae.partilhae.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Não existe endpoint de signup aqui de propósito: cadastrar um
 * Doador ou Receptor já é feito por POST /doadores e
 * POST /beneficiarios (ambos liberados no WebSecurityConfig), então não
 * faz sentido duplicar essa lógica em /auth/signup.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Autentica um Doador ou Receptor pelo e-mail e senha, retornando um token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    })
    public ResponseEntity<JwtResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        String role = principal.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse(null);

        return ResponseEntity.ok(new JwtResponseDTO(
                jwt,
                "Bearer",
                principal.getIdUsuario(),
                principal.getNomeUsuario(),
                principal.getUsername(),
                role,
                principal.getIdDoador(),
                principal.getIdReceptor()
        ));
    }
}
