package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.DoadorRepository;
import br.com.partilhae.partilhae.repository.BeneficiarioRepository;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.security.UserDetailsImpl;
import br.com.partilhae.partilhae.support.DadosDeTeste;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock DoadorRepository doadorRepository;
    @Mock BeneficiarioRepository beneficiarioRepository;

    @Test
    @DisplayName("Carrega doador com papel e identificador de perfil corretos")
    void carregaDoador() {
        Usuario usuario = usuario(1L, "DOADOR", "doador@partilhae.test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(doadorRepository.findByUsuario_IdUsuario(1L)).thenReturn(Optional.of(DadosDeTeste.doador(11L, usuario)));
        UserDetailsImpl detalhes = (UserDetailsImpl) service("").loadUserByUsername(usuario.getEmail());
        assertThat(detalhes.getIdDoador()).isEqualTo(11L);
        assertThat(detalhes.getIdReceptor()).isNull();
        assertThat(detalhes.getAuthorities()).extracting("authority").containsExactly("ROLE_DOADOR");
    }

    @Test
    @DisplayName("Carrega receptor com papel e identificador de perfil corretos")
    void carregaReceptor() {
        Usuario usuario = usuario(2L, "RECEPTOR", "receptor@partilhae.test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(beneficiarioRepository.findByUsuario_IdUsuario(2L)).thenReturn(Optional.of(DadosDeTeste.beneficiario(22L, usuario)));
        UserDetailsImpl detalhes = (UserDetailsImpl) service("").loadUserByUsername(usuario.getEmail());
        assertThat(detalhes.getIdDoador()).isNull();
        assertThat(detalhes.getIdReceptor()).isEqualTo(22L);
        assertThat(detalhes.getAuthorities()).extracting("authority").containsExactly("ROLE_RECEPTOR");
    }

    @Test
    @DisplayName("Rejeita usuário inexistente")
    void usuarioInexistente() {
        when(usuarioRepository.findByEmailIgnoreCase("ausente@partilhae.test")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service("").loadUserByUsername("ausente@partilhae.test"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Rejeita perfil DOADOR sem registro correspondente")
    void perfilDoadorInexistente() {
        Usuario usuario = usuario(3L, "DOADOR", "sem-perfil@partilhae.test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(doadorRepository.findByUsuario_IdUsuario(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service("").loadUserByUsername(usuario.getEmail()))
                .isInstanceOf(UsernameNotFoundException.class).hasMessage("Perfil de doador não encontrado");
    }

    @Test
    @DisplayName("Rejeita perfil RECEPTOR sem registro correspondente")
    void perfilReceptorInexistente() {
        Usuario usuario = usuario(30L, "RECEPTOR", "sem-receptor@partilhae.test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(beneficiarioRepository.findByUsuario_IdUsuario(30L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service("").loadUserByUsername(usuario.getEmail()))
                .isInstanceOf(UsernameNotFoundException.class).hasMessage("Perfil de receptor não encontrado");
    }

    @Test
    @DisplayName("Rejeita perfil que não corresponde a papel conhecido")
    void perfilInvalido() {
        Usuario usuario = usuario(4L, "OPERADOR", "invalido@partilhae.test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertThatThrownBy(() -> service("").loadUserByUsername(usuario.getEmail()))
                .isInstanceOf(UsernameNotFoundException.class).hasMessage("Perfil de usuário inválido");
    }

    @Test
    @DisplayName("Reconhece administrador previamente provisionado e autorizado")
    void reconheceAdminProvisionado() {
        Usuario usuario = usuario(5L, "ADMIN", "admin@partilhae.test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        UserDetailsImpl detalhes = (UserDetailsImpl) service("admin@partilhae.test")
                .loadUserByUsername(usuario.getEmail());
        assertThat(detalhes.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Compara e-mails administrativos sem diferenciar maiúsculas ou espaços")
    void emailAdminIgnoraMaiusculas() {
        Usuario usuario = usuario(6L, "ADMIN", "Admin@Partilhae.Test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        UserDetailsImpl detalhes = (UserDetailsImpl) service(" admin@partilhae.test , outro@partilhae.test ")
                .loadUserByUsername(usuario.getEmail());
        assertThat(detalhes.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("E-mail configurado não transforma conta pública em administrador")
    void emailConfiguradoNaoElevaContaPublica() {
        Usuario usuario = usuario(7L, "DOADOR", "admin@partilhae.test");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(doadorRepository.findByUsuario_IdUsuario(7L)).thenReturn(Optional.of(DadosDeTeste.doador(77L, usuario)));
        UserDetailsImpl detalhes = (UserDetailsImpl) service("admin@partilhae.test")
                .loadUserByUsername(usuario.getEmail());
        assertThat(detalhes.getAuthorities()).extracting("authority").containsExactly("ROLE_DOADOR");
    }

    @Test
    @DisplayName("Conta bloqueada ou inativa não fica habilitada para autenticação")
    void contaBloqueadaOuInativa() {
        Usuario bloqueado = usuario(8L, "DOADOR", "bloqueado@partilhae.test");
        bloqueado.setStatusConta("BLOQUEADA");
        Usuario inativo = usuario(9L, "RECEPTOR", "inativo@partilhae.test");
        inativo.setStatusConta("INATIVA");
        when(usuarioRepository.findByEmailIgnoreCase(bloqueado.getEmail())).thenReturn(Optional.of(bloqueado));
        when(doadorRepository.findByUsuario_IdUsuario(8L)).thenReturn(Optional.of(DadosDeTeste.doador(88L, bloqueado)));
        when(usuarioRepository.findByEmailIgnoreCase(inativo.getEmail())).thenReturn(Optional.of(inativo));
        when(beneficiarioRepository.findByUsuario_IdUsuario(9L)).thenReturn(Optional.of(DadosDeTeste.beneficiario(99L, inativo)));
        UserDetailsImpl blockedDetails = (UserDetailsImpl) service("").loadUserByUsername(bloqueado.getEmail());
        UserDetailsImpl inactiveDetails = (UserDetailsImpl) service("").loadUserByUsername(inativo.getEmail());
        assertThat(blockedDetails.isEnabled()).isFalse();
        assertThat(blockedDetails.isAccountNonLocked()).isFalse();
        assertThat(inactiveDetails.isEnabled()).isFalse();
    }

    private UserDetailsServiceImpl service(String adminEmails) {
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(usuarioRepository, doadorRepository, beneficiarioRepository);
        ReflectionTestUtils.setField(service, "adminEmails", adminEmails);
        return service;
    }

    private static Usuario usuario(Long id, String perfil, String email) {
        return DadosDeTeste.usuario(id, email, perfil);
    }
}
