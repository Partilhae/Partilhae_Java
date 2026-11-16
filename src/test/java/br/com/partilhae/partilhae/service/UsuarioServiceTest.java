package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.UsuarioRequestDTO;
import br.com.partilhae.partilhae.exception.DadoDuplicadoException;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.support.DadosDeTeste;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    void preparar() {
        service = new UsuarioService(usuarioRepository, passwordEncoder, "admin@partilhae.test");
    }

    @AfterEach
    void limparSeguranca() {
        DadosDeTeste.deslogar();
    }

    @Test
    @DisplayName("Cadastra usuário válido com senha armazenada por hash e CPF válido")
    void cadastroValido() {
        when(passwordEncoder.encode("Senha@123")).thenReturn("hash-bcrypt");
        Usuario resultado = service.criar("Maria Teste", "maria@partilhae.test", "Senha@123",
                "529.982.247-25", "11999999999", "01001000", "Rua de Teste", "10", null,
                "Centro", "São Paulo", "SP", "DOADOR");

        assertThat(resultado.getSenha()).isEqualTo("hash-bcrypt").isNotEqualTo("Senha@123");
        assertThat(resultado.getCpf()).isEqualTo("52998224725");
        assertThat(resultado.getPerfil()).isEqualTo("DOADOR");
        assertThat(resultado.getStatusConta()).isEqualTo("ATIVA");
    }

    @Test
    @DisplayName("Aceita CPF ficticio com onze digitos")
    void cpfFicticioComOnzeDigitos() {
        assertThat(criar("12345678901", "cpf-ficticio@partilhae.test").getCpf()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("Aceita CPF com onze digitos repetidos")
    void cpfComDigitosRepetidos() {
        assertThat(criar("11111111111", "cpf-repetido@partilhae.test").getCpf()).isEqualTo("11111111111");
    }

    @Test
    @DisplayName("Rejeita CPF com menos ou mais de onze digitos apos normalizacao")
    void cpfComQuantidadeDeDigitosInvalida() {
        assertThatThrownBy(() -> criar("123.456.789-0", "cpf-curto@partilhae.test"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("CPF inválido");
        assertThatThrownBy(() -> criar("123.456.789-012", "cpf-longo@partilhae.test"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("CPF inválido");
    }

    @Test
    @DisplayName("Rejeita e-mail duplicado")
    void emailDuplicado() {
        when(usuarioRepository.findByEmailIgnoreCase("maria@partilhae.test"))
                .thenReturn(Optional.of(DadosDeTeste.usuario(9L)));
        assertThatThrownBy(() -> criar("52998224725", "maria@partilhae.test"))
                .isInstanceOf(DadoDuplicadoException.class).hasMessage("E-mail já cadastrado");
    }

    @Test
    @DisplayName("Rejeita CPF duplicado")
    void cpfDuplicado() {
        when(usuarioRepository.findByEmailIgnoreCase("novo@partilhae.test")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCpf("52998224725")).thenReturn(Optional.of(DadosDeTeste.usuario(9L)));
        assertThatThrownBy(() -> criar("52998224725", "novo@partilhae.test"))
                .isInstanceOf(DadoDuplicadoException.class).hasMessage("CPF já cadastrado");
    }

    @Test
    @DisplayName("Não permite cadastro público com papel ADMIN ou e-mail reservado")
    void cadastroPublicoNaoConcedeAdmin() {
        assertThatThrownBy(() -> service.criar("Admin", "novo@partilhae.test", "Senha@123",
                "52998224725", "11999999999", "01001000", "Rua", "10", null,
                "Centro", "São Paulo", "SP", "ADMIN"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> criar("52998224725", "ADMIN@partilhae.test"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("E-mail reservado para administração");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Atualiza somente os campos enviados")
    void atualizacaoParcial() {
        Usuario usuario = DadosDeTeste.usuario(1L);
        usuario.setNomeUsuario("Nome original");
        usuario.setCidade("Cidade original");

        service.atualizarParcial(usuario, "Nome atualizado", null, null, null, null, null,
                null, null, null, null, null, null);

        assertThat(usuario.getNomeUsuario()).isEqualTo("Nome atualizado");
        assertThat(usuario.getEmail()).isEqualTo("usuario1@partilhae.test");
        assertThat(usuario.getCidade()).isEqualTo("Cidade original");
    }

    @Test
    @DisplayName("Atualização sem campos de senha não altera a senha")
    void atualizacaoPreservaCamposAusentes() {
        Usuario usuario = DadosDeTeste.usuario(1L);
        String senhaOriginal = usuario.getSenha();
        service.atualizarParcial(usuario, null, null, null, null, null, null,
                null, null, null, null, null, null);
        assertThat(usuario.getSenha()).isEqualTo(senhaOriginal);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Criptografa uma nova senha durante a atualização")
    void atualizarSenha() {
        Usuario usuario = DadosDeTeste.usuario(1L);
        when(passwordEncoder.encode("OutraSenha@123")).thenReturn("novo-hash");
        service.atualizarParcial(usuario, null, null, "OutraSenha@123", null, null, null,
                null, null, null, null, null, null);
        assertThat(usuario.getSenha()).isEqualTo("novo-hash");
        verify(passwordEncoder).encode("OutraSenha@123");
    }

    @Test
    @DisplayName("Permite manter o próprio e-mail e CPF")
    void atualizarMantendoEmailECpf() {
        Usuario usuario = DadosDeTeste.usuario(1L);
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
        service.atualizarParcial(usuario, "Nome novo", usuario.getEmail(), null, usuario.getCpf(),
                null, null, null, null, null, null, null, null);
        assertThat(usuario.getNomeUsuario()).isEqualTo("Nome novo");
    }

    @Test
    @DisplayName("Rejeita e-mail ou CPF pertencente a outra pessoa")
    void atualizarComDadosDeOutraPessoa() {
        Usuario usuario = DadosDeTeste.usuario(1L);
        Usuario outraPessoa = DadosDeTeste.usuario(2L);
        when(usuarioRepository.findByEmailIgnoreCase("outra@partilhae.test"))
                .thenReturn(Optional.of(outraPessoa));
        assertThatThrownBy(() -> service.atualizarParcial(usuario, null, "outra@partilhae.test",
                null, null, null, null, null, null, null, null, null, null))
                .isInstanceOf(DadoDuplicadoException.class).hasMessage("E-mail já cadastrado");

        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(outraPessoa));
        assertThatThrownBy(() -> service.atualizarParcial(usuario, null, null,
                null, "11144477735", null, null, null, null, null, null, null, null))
                .isInstanceOf(DadoDuplicadoException.class).hasMessage("CPF já cadastrado");
    }

    @Test
    @DisplayName("Impede que um usuário consulte outro cadastro")
    void consultarOutroUsuario() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(DadosDeTeste.usuario(2L)));
        assertThatThrownBy(() -> service.buscarPorId(2L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Impede que um usuário atualize outro cadastro")
    void atualizarOutroUsuario() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(DadosDeTeste.usuario(2L)));
        UsuarioRequestDTO dto = new UsuarioRequestDTO("Tentativa", null, null, null, null, null,
                null, null, null, null, null, null);
        assertThatThrownBy(() -> service.atualizar(2L, dto)).isInstanceOf(AccessDeniedException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Impede que um usuário exclua outro cadastro")
    void excluirOutroUsuario() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(DadosDeTeste.usuario(2L)));
        assertThatThrownBy(() -> service.excluir(2L)).isInstanceOf(AccessDeniedException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Desativa a conta do titular sem excluir o registro")
    void excluirLogicamente() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        Usuario usuario = DadosDeTeste.usuario(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        service.excluir(1L);
        assertThat(usuario.getStatusConta()).isEqualTo("BLOQUEADA");
        verify(usuarioRepository).save(usuario);
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Somente administrador altera status e valores inválidos são rejeitados")
    void atualizarStatus() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        assertThatThrownBy(() -> service.atualizarStatus(2L, "ATIVA"))
                .isInstanceOf(AccessDeniedException.class);
        DadosDeTeste.logarComoAdmin(90L);
        assertThatThrownBy(() -> service.atualizarStatus(2L, "PENDENTE"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Status de conta inválido");
        verify(usuarioRepository, never()).findById(2L);
    }

    @Test
    @DisplayName("Retorna não encontrado ao consultar usuário inexistente")
    void usuarioInexistente() {
        DadosDeTeste.logarComoAdmin(90L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private Usuario criar(String cpf, String email) {
        return service.criar("Maria Teste", email, "Senha@123", cpf, "11999999999", "01001000",
                "Rua de Teste", "10", null, "Centro", "São Paulo", "SP", "DOADOR");
    }
}
