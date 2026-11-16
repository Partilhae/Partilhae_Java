package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.DoadorRepository;
import br.com.partilhae.partilhae.repository.BeneficiarioRepository;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final DoadorRepository doadorRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    @Value("${APP_ADMIN_EMAILS:}")
    private String adminEmails;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String perfil = usuario.getPerfil().toUpperCase(Locale.ROOT);
        Long idDoador = null;
        Long idReceptor = null;
        boolean administrador = false;
        if ("DOADOR".equals(perfil)) {
            idDoador = doadorRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new UsernameNotFoundException("Perfil de doador não encontrado"))
                    .getIdDoador();
        } else if ("RECEPTOR".equals(perfil)) {
            idReceptor = beneficiarioRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new UsernameNotFoundException("Perfil de receptor não encontrado"))
                    .getIdReceptor();
        } else if ("ADMIN".equals(perfil) && isAdminEmail(usuario.getEmail())) {
            administrador = true;
        } else {
            throw new UsernameNotFoundException("Perfil de usuário inválido");
        }

        return UserDetailsImpl.fromUsuario(usuario, idDoador, idReceptor, administrador);
    }

    private boolean isAdminEmail(String email) {
        if (adminEmails == null || adminEmails.isBlank()) return false;
        return java.util.Arrays.stream(adminEmails.split(","))
                .map(String::trim).anyMatch(config -> config.equalsIgnoreCase(email));
    }
}
