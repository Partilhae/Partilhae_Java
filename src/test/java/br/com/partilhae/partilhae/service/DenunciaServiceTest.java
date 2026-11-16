package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.DenunciaRequestDTO;
import br.com.partilhae.partilhae.dto.DenunciaResponseDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.model.Denuncia;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.DenunciaRepository;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.support.DadosDeTeste;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DenunciaServiceTest {
    @Mock DenunciaRepository denunciaRepository;
    @Mock UsuarioRepository usuarioRepository;
    private DenunciaService service;

    @BeforeEach
    void preparar() {
        service = new DenunciaService(denunciaRepository, usuarioRepository);
    }

    @AfterEach
    void limparSeguranca() {
        DadosDeTeste.deslogar();
    }

    @Test
    @DisplayName("Cadastra denúncia contra usuário com denunciante obtido da autenticação")
    void denunciaUsuario() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        Usuario denunciante = usuario(1L);
        Usuario denunciado = usuario(2L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(denunciante));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(denunciado));
        salvarDenuncia();
        DenunciaResponseDTO resposta = service.cadastrar(pedido(2L, null, null, null));
        assertThat(resposta.idDenunciante()).isEqualTo(1L);
        assertThat(resposta.idUsuarioDenunciado()).isEqualTo(2L);
        assertThat(resposta.status()).isEqualTo("ABERTA");
    }

    @Test
    @DisplayName("Armazena denúncias de conversa, solicitação e doação sem integração externa")
    void alvosExternos() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        salvarDenuncia();

        DenunciaResponseDTO conversa = service.cadastrar(pedido(null, 21L, null, null));
        DenunciaResponseDTO solicitacao = service.cadastrar(pedido(null, null, 31L, null));
        DenunciaResponseDTO doacao = service.cadastrar(pedido(null, null, null, 41L));

        assertThat(conversa.idConversa()).isEqualTo(21L);
        assertThat(solicitacao.idSolicitacao()).isEqualTo(31L);
        assertThat(doacao.idDoacao()).isEqualTo(41L);
        verify(denunciaRepository, times(3)).save(any(Denuncia.class));
        verify(usuarioRepository, times(3)).findById(1L);
        verify(usuarioRepository, never()).findById(21L);
        verify(usuarioRepository, never()).findById(31L);
        verify(usuarioRepository, never()).findById(41L);
    }

    @Test
    @DisplayName("Rejeita autodenúncia")
    void autodenuncia() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        assertThatThrownBy(() -> service.cadastrar(pedido(1L, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Não é permitido denunciar a própria conta");
        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita usuário denunciado inexistente")
    void usuarioDenunciadoInexistente() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cadastrar(pedido(99L, null, null, null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita denunciante com conta bloqueada")
    void denuncianteBloqueado() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        Usuario denunciante = usuario(1L);
        denunciante.setStatusConta("BLOQUEADA");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(denunciante));
        assertThatThrownBy(() -> service.cadastrar(pedido(null, null, null, 41L)))
                .isInstanceOf(AccessDeniedException.class);
        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita denúncia sem alvo ou com mais de um alvo mesmo fora do controller")
    void alvoAusenteOuMultiplo() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        assertThatThrownBy(() -> service.cadastrar(pedido(null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cadastrar(pedido(2L, 21L, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cadastrar(pedido(null, 21L, 31L, 41L)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioRepository, never()).findById(any());
        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lista denúncias somente para administrador")
    void listagemAdministrativa() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        PageRequest pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> service.listar(pageable)).isInstanceOf(AccessDeniedException.class);
        DadosDeTeste.logarComoAdmin(90L);
        when(denunciaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(
                DadosDeTeste.denunciaDoacao(5L, usuario(1L), 41L)), pageable, 1));
        assertThat(service.listar(pageable).getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Denunciante consulta a própria denúncia")
    void consultaPropriaDenuncia() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(denunciaRepository.findById(5L)).thenReturn(Optional.of(
                DadosDeTeste.denunciaDoacao(5L, usuario(1L), 41L)));
        assertThat(service.buscar(5L).idDenunciante()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Impede consulta de denúncia pertencente a outra pessoa")
    void consultaDeOutraPessoa() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(denunciaRepository.findById(5L)).thenReturn(Optional.of(
                DadosDeTeste.denunciaDoacao(5L, usuario(2L), 41L)));
        assertThatThrownBy(() -> service.buscar(5L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Usuário lista somente as próprias denúncias")
    void listaPorTitular() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        PageRequest pageable = PageRequest.of(0, 10);
        when(denunciaRepository.findByDenunciante_IdUsuarioOrderByDataDenunciaDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(DadosDeTeste.denunciaDoacao(5L, usuario(1L), 41L)), pageable, 1));
        assertThat(service.listarPorUsuario(1L, pageable).getTotalElements()).isEqualTo(1);
        assertThatThrownBy(() -> service.listarPorUsuario(2L, pageable)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Administrador altera status permitido e usuário comum não altera")
    void atualizarStatus() {
        Denuncia denuncia = DadosDeTeste.denunciaDoacao(5L, usuario(1L), 41L);
        DadosDeTeste.logarComoDoador(1L, 10L);
        assertThatThrownBy(() -> service.atualizarStatus(5L, "EM_ANALISE"))
                .isInstanceOf(AccessDeniedException.class);
        DadosDeTeste.logarComoAdmin(90L);
        when(denunciaRepository.findById(5L)).thenReturn(Optional.of(denuncia));
        when(denunciaRepository.save(denuncia)).thenReturn(denuncia);
        assertThat(service.atualizarStatus(5L, "EM_ANALISE").status()).isEqualTo("EM_ANALISE");
        assertThatThrownBy(() -> service.atualizarStatus(5L, "PENDENTE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Registra resolução e muda o status para RESOLVIDA")
    void atualizarResolucao() {
        DadosDeTeste.logarComoAdmin(90L);
        Denuncia denuncia = DadosDeTeste.denunciaDoacao(5L, usuario(1L), 41L);
        when(denunciaRepository.findById(5L)).thenReturn(Optional.of(denuncia));
        when(denunciaRepository.save(denuncia)).thenReturn(denuncia);
        DenunciaResponseDTO resposta = service.atualizarResolucao(5L, "  Revisada pela equipe.  ");
        assertThat(resposta.resolucao()).isEqualTo("Revisada pela equipe.");
        assertThat(resposta.status()).isEqualTo("RESOLVIDA");
    }

    @Test
    @DisplayName("Rejeita resolução vazia ou nula")
    void resolucaoVazia() {
        DadosDeTeste.logarComoAdmin(90L);
        assertThatThrownBy(() -> service.atualizarResolucao(5L, "  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.atualizarResolucao(5L, null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(denunciaRepository, never()).findById(any());
        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Impede usuário comum de registrar resolução")
    void resolucaoPorUsuarioComum() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        assertThatThrownBy(() -> service.atualizarResolucao(5L, "Revisada"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Retorna não encontrado ao resolver denúncia inexistente")
    void denunciaInexistente() {
        DadosDeTeste.logarComoAdmin(90L);
        when(denunciaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizarResolucao(99L, "Revisada"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Status inicial de denúncia é ABERTA")
    void statusInicial() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        salvarDenuncia();
        assertThat(service.cadastrar(pedido(null, null, 31L, null)).status()).isEqualTo("ABERTA");
    }

    private void salvarDenuncia() {
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(invocacao -> {
            Denuncia denuncia = invocacao.getArgument(0);
            denuncia.setIdDenuncia(5L);
            denuncia.setDataDenuncia(LocalDateTime.of(2026, 11, 1, 10, 0));
            return denuncia;
        });
    }

    private static DenunciaRequestDTO pedido(Long usuario, Long conversa, Long solicitacao, Long doacao) {
        return new DenunciaRequestDTO(usuario, conversa, solicitacao, doacao, "Relato objetivo");
    }

    private static Usuario usuario(Long id) {
        Usuario usuario = DadosDeTeste.usuario(id);
        usuario.setStatusConta("ATIVA");
        return usuario;
    }
}
