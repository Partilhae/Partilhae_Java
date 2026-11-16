package br.com.partilhae.partilhae.security;

import br.com.partilhae.partilhae.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ArrayList;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long idUsuario;
    private final String nomeUsuario;
    private final String email;

    @JsonIgnore
    private final String senha;

    private final Long idDoador;
    private final Long idReceptor;
    private final boolean enabled;
    private final boolean accountNonLocked;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long idUsuario, String nomeUsuario, String email, String senha,
                           Long idDoador, Long idReceptor, boolean enabled, boolean accountNonLocked,
                           Collection<? extends GrantedAuthority> authorities) {
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.email = email;
        this.senha = senha;
        this.idDoador = idDoador;
        this.idReceptor = idReceptor;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    public static UserDetailsImpl fromUsuario(Usuario usuario, Long idDoador, Long idReceptor, boolean admin) {
        String perfil = usuario.getPerfil().toUpperCase(Locale.ROOT);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(admin ? "ROLE_ADMIN" : "ROLE_" + perfil));
        return new UserDetailsImpl(
                usuario.getIdUsuario(),
                usuario.getNomeUsuario(),
                usuario.getEmail(),
                usuario.getSenha(),
                idDoador,
                idReceptor,
                "ATIVA".equalsIgnoreCase(usuario.getStatusConta()),
                !"BLOQUEADA".equalsIgnoreCase(usuario.getStatusConta()),
                grantedAuthorities
        );
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public Long getIdDoador() {
        return idDoador;
    }

    public Long getIdReceptor() {
        return idReceptor;
    }

    public boolean isDoador() {
        return idDoador != null;
    }

    public boolean isReceptor() {
        return idReceptor != null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    /**
     * O "username" do Spring Security aqui é o e-mail — é o que usamos
     * para login (não existe campo "username" separado no nosso domínio).
     */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(idUsuario, that.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }
}
