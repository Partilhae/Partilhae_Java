package br.com.partilhae.partilhae.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserDetailsImpl getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return usuario;
    }

    public static void validarPosseDoador(Long idDoadorDoRecurso) {
        UserDetailsImpl usuario = getUsuarioLogado();
        if (!isAdmin(usuario) && (!usuario.isDoador() || !Objects.equals(usuario.getIdDoador(), idDoadorDoRecurso))) {
            throw new AccessDeniedException("Você não tem permissão para acessar este recurso");
        }
    }

    public static void validarPosseReceptor(Long idReceptorDoRecurso) {
        UserDetailsImpl usuario = getUsuarioLogado();
        if (!isAdmin(usuario) && (!usuario.isReceptor() || !Objects.equals(usuario.getIdReceptor(), idReceptorDoRecurso))) {
            throw new AccessDeniedException("Você não tem permissão para acessar este recurso");
        }
    }

    public static boolean isAdmin() { return isAdmin(getUsuarioLogado()); }

    private static boolean isAdmin(UserDetailsImpl user) {
        return user.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public static void validarPosseUsuario(Long idUsuario) {
        UserDetailsImpl logado = getUsuarioLogado();
        if (!isAdmin(logado) && !Objects.equals(logado.getIdUsuario(), idUsuario))
            throw new AccessDeniedException("Você não tem permissão para acessar este usuário");
    }

    public static boolean podeVerEndereco(Long idUsuario) {
        UserDetailsImpl logado = getUsuarioLogado();
        return isAdmin(logado) || Objects.equals(logado.getIdUsuario(), idUsuario);
    }
}
