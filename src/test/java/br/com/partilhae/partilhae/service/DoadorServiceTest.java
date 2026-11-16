package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.DoadorRequestDTO;
import br.com.partilhae.partilhae.dto.DoadorResponseDTO;
import br.com.partilhae.partilhae.dto.DoadorUpdateRequestDTO;
import br.com.partilhae.partilhae.model.Doador;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.DoadorRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoadorServiceTest {
    @Mock DoadorRepository doadorRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock UsuarioService usuarioService;

    private DoadorService service;

    @BeforeEach
    void preparar() {
        service = new DoadorService(doadorRepository, usuarioRepository, usuarioService);
    }

    @AfterEach
    void limparSeguranca() {
        DadosDeTeste.deslogar();
    }

    @Test
    @DisplayName("Cadastra pessoa física sem nome de estabelecimento")
    void cadastroPessoaFisica() {
        prepararCadastro();
        DoadorResponseDTO resposta = service.cadastrar(pedido("pessoaFisica", null));
        assertThat(resposta.tipoDoador()).isEqualTo("pessoaFisica");
        assertThat(resposta.nomeEstabelecimento()).isNull();
    }

    @Test
    @DisplayName("Cadastra estabelecimento com nome informado")
    void cadastroEstabelecimento() {
        prepararCadastro();
        DoadorResponseDTO resposta = service.cadastrar(pedido("estabelecimento", "  Mercado Teste  "));
        assertThat(resposta.nomeEstabelecimento()).isEqualTo("Mercado Teste");
    }

    @Test
    @DisplayName("Cadastra projeto social com nome informado")
    void cadastroProjetoSocial() {
        prepararCadastro();
        DoadorResponseDTO resposta = service.cadastrar(pedido("projetoSocial", "Projeto Teste"));
        assertThat(resposta.tipoDoador()).isEqualTo("projetoSocial");
        assertThat(resposta.nomeEstabelecimento()).isEqualTo("Projeto Teste");
    }

    @Test
    @DisplayName("Exige nomeEstabelecimento para estabelecimento e projeto social")
    void nomeObrigatorio() {
        assertThatThrownBy(() -> service.cadastrar(pedido("estabelecimento", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cadastrar(pedido("projetoSocial", "   ")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cadastrar(pedido("empresa", "Empresa Teste")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioService, never()).criar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Proíbe nomeEstabelecimento para pessoa física")
    void nomeProibidoParaPessoaFisica() {
        assertThatThrownBy(() -> service.cadastrar(pedido("pessoaFisica", "Mercado")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita combinações inválidas de tipo e nome em uma atualização")
    void atualizacaoComTipoInvalido() {
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doadorTitular()));
        assertThatThrownBy(() -> service.atualizar(10L, atualizacao(null, null, "estabelecimento", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.atualizar(10L, atualizacao(null, null, "pessoaFisica", "Mercado")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioService, never()).atualizarParcial(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Atualiza disponibilidade e observações sem alterar os demais campos")
    void atualizarDisponibilidadeEObservacao() {
        Doador doador = doadorTitular();
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doador));
        when(doadorRepository.save(doador)).thenReturn(doador);
        DoadorUpdateRequestDTO dto = atualizacao("Durante a semana", "Combinar retirada", null, null);

        DoadorResponseDTO resposta = service.atualizar(10L, dto);
        assertThat(resposta.disponibilidade()).isEqualTo("Durante a semana");
        assertThat(resposta.observacao()).isEqualTo("Combinar retirada");
        assertThat(doador.getTipoDoador()).isEqualTo("pessoaFisica");
    }

    @Test
    @DisplayName("Atualiza dados pessoais do titular")
    void atualizarDadosPessoais() {
        Doador doador = doadorTitular();
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doador));
        when(doadorRepository.save(doador)).thenReturn(doador);
        service.atualizar(10L, new DoadorUpdateRequestDTO("Nome atualizado", null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null));
        verify(usuarioService).atualizarParcial(doador.getUsuario(), "Nome atualizado", null, null, null,
                null, null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Permite mudar tipo e nome de pessoa física para estabelecimento")
    void atualizarTipoDoador() {
        Doador doador = doadorTitular();
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doador));
        when(doadorRepository.save(doador)).thenReturn(doador);
        DoadorResponseDTO resposta = service.atualizar(10L, new DoadorUpdateRequestDTO(null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                "estabelecimento", "Mercado Teste"));
        assertThat(resposta.tipoDoador()).isEqualTo("estabelecimento");
        assertThat(resposta.nomeEstabelecimento()).isEqualTo("Mercado Teste");
    }

    @Test
    @DisplayName("Limpa nomeEstabelecimento ao mudar para pessoa física")
    void voltarParaPessoaFisica() {
        Doador doador = doadorTitular();
        doador.setTipoDoador("estabelecimento");
        doador.setNomeEstabelecimento("Mercado Teste");
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doador));
        when(doadorRepository.save(doador)).thenReturn(doador);
        service.atualizar(10L, new DoadorUpdateRequestDTO(null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, "pessoaFisica", null));
        assertThat(doador.getNomeEstabelecimento()).isNull();
    }

    @Test
    @DisplayName("Titular e administrador consultam dados privados do perfil")
    void consultaTitularEAdministrador() {
        Doador doador = doadorTitular();
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doador));
        DadosDeTeste.logarComoDoador(1L, 10L);
        assertThat(service.buscarPorId(10L).email()).isEqualTo("usuario1@partilhae.test");
        DadosDeTeste.logarComoAdmin(90L);
        assertThat(service.buscarPorId(10L).endereco()).isEqualTo("Rua de Teste");
    }

    @Test
    @DisplayName("Oculta e-mail, telefone e endereço de perfil consultado por outro usuário")
    void consultaPerfilDeOutroUsuario() {
        Doador doador = doadorTitular();
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doador));
        DadosDeTeste.logarComoReceptor(2L, 20L);
        DoadorResponseDTO resposta = service.buscarPorId(10L);
        assertThat(resposta.nomeUsuario()).isEqualTo("Usuário de Teste 1");
        assertThat(resposta.email()).isNull();
        assertThat(resposta.telefone()).isNull();
        assertThat(resposta.cep()).isNull();
        assertThat(resposta.endereco()).isNull();
        assertThat(resposta.numero()).isNull();
    }

    @Test
    @DisplayName("Lista administrativa mantém contato visível e endereço oculto")
    void listagemAdministrativa() {
        DadosDeTeste.logarComoAdmin(90L);
        Doador doador = doadorTitular();
        when(doadorRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(doador), PageRequest.of(0, 10), 1));
        DoadorResponseDTO resposta = service.listar(PageRequest.of(0, 10)).getContent().getFirst();
        assertThat(resposta.email()).isEqualTo("usuario1@partilhae.test");
        assertThat(resposta.telefone()).isEqualTo("11900000000");
        assertThat(resposta.endereco()).isNull();
        assertThat(resposta.cep()).isNull();
    }

    @Test
    @DisplayName("Impede atualização do perfil de outro doador")
    void naoAtualizaOutroDoador() {
        DadosDeTeste.logarComoDoador(2L, 20L);
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doadorTitular()));
        assertThatThrownBy(() -> service.atualizar(10L, atualizacao(null, null, null, null)))
                .isInstanceOf(AccessDeniedException.class);
        verify(doadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Desativa o perfil bloqueando a conta sem excluir o registro")
    void desativarSemExcluirFisicamente() {
        Doador doador = doadorTitular();
        DadosDeTeste.logarComoDoador(1L, 10L);
        when(doadorRepository.findById(10L)).thenReturn(Optional.of(doador));
        service.desativar(10L);
        assertThat(doador.getUsuario().getStatusConta()).isEqualTo("BLOQUEADA");
        verify(usuarioRepository).save(doador.getUsuario());
        verify(doadorRepository, never()).delete(any());
    }

    private void prepararCadastro() {
        Usuario usuario = DadosDeTeste.usuario(1L);
        when(usuarioService.criar("Doador Teste", "novo@partilhae.test", "Senha@123", "52998224725",
                "11999999999", "01001000", "Rua de Teste", "10", null, "Centro", "São Paulo", "SP", "DOADOR"))
                .thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(doadorRepository.save(any(Doador.class))).thenAnswer(invocacao -> {
            Doador salvo = invocacao.getArgument(0);
            salvo.setIdDoador(10L);
            return salvo;
        });
    }

    private static DoadorRequestDTO pedido(String tipo, String nome) {
        return new DoadorRequestDTO("Doador Teste", "novo@partilhae.test", "Senha@123", "52998224725",
                "11999999999", "01001000", "Rua de Teste", "10", null, "Centro", "São Paulo", "SP",
                "Finais de semana", "Retirada combinada", tipo, nome);
    }

    private static DoadorUpdateRequestDTO atualizacao(String disponibilidade, String observacao,
                                                       String tipo, String nome) {
        return new DoadorUpdateRequestDTO(null, null, null, null, null, null, null, null,
                null, null, null, null, disponibilidade, observacao, tipo, nome);
    }

    private static Doador doadorTitular() {
        return DadosDeTeste.doador(10L, DadosDeTeste.usuario(1L));
    }
}
