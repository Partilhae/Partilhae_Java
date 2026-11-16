package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.ReceptorRequestDTO;
import br.com.partilhae.partilhae.dto.ReceptorResponseDTO;
import br.com.partilhae.partilhae.dto.ReceptorUpdateRequestDTO;
import br.com.partilhae.partilhae.model.Receptor;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.ReceptorRepository;
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
class ReceptorServiceTest {
    @Mock ReceptorRepository receptorRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock UsuarioService usuarioService;
    private ReceptorService service;

    @BeforeEach
    void preparar() {
        service = new ReceptorService(receptorRepository, usuarioRepository, usuarioService);
    }

    @AfterEach
    void limparSeguranca() {
        DadosDeTeste.deslogar();
    }

    @Test
    @DisplayName("Cadastra pessoa física sem nome de organização")
    void cadastroPessoaFisica() {
        prepararCadastro();
        ReceptorResponseDTO resposta = service.cadastrar(pedido("pessoaFisica", null, true));
        assertThat(resposta.tipoBeneficiario()).isEqualTo("pessoaFisica");
        assertThat(resposta.nomeOrganizacao()).isNull();
    }

    @Test
    @DisplayName("Cadastra ONG com nome de organização")
    void cadastroOng() {
        prepararCadastro();
        ReceptorResponseDTO resposta = service.cadastrar(pedido("ONG", "  ONG Teste  ", false));
        assertThat(resposta.tipoBeneficiario()).isEqualTo("ONG");
        assertThat(resposta.nomeOrganizacao()).isEqualTo("ONG Teste");
        assertThat(resposta.possuiCriancas()).isFalse();
    }

    @Test
    @DisplayName("Exige nomeOrganizacao para ONG e impede nome para pessoa física")
    void regrasDeNomeDaOrganizacao() {
        assertThatThrownBy(() -> service.cadastrar(pedido("ONG", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cadastrar(pedido("pessoaFisica", "ONG Teste", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cadastrar(pedido("associacao", "Associação Teste", null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioService, never()).criar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Cadastra crianças presentes e ausentes sem perder o valor falso")
    void cadastroComESeCriancas() {
        prepararCadastro();
        assertThat(service.cadastrar(pedido("pessoaFisica", null, true)).possuiCriancas()).isTrue();
        prepararCadastro();
        assertThat(service.cadastrar(pedido("pessoaFisica", null, false)).possuiCriancas()).isFalse();
    }

    @Test
    @DisplayName("Atualiza quantidade de pessoas e presença de crianças")
    void atualizarDadosFamiliares() {
        Receptor receptor = receptorTitular();
        DadosDeTeste.logarComoReceptor(1L, 20L);
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptor));
        when(receptorRepository.save(receptor)).thenReturn(receptor);
        service.atualizar(20L, atualizacao(null, 6, false, null, null, null, null));
        assertThat(receptor.getQuantidadePessoasFamilia()).isEqualTo(6);
        assertThat(receptor.getPossuiCriancas()).isFalse();
    }

    @Test
    @DisplayName("Atualiza restrição alimentar")
    void atualizarRestricao() {
        Receptor receptor = receptorTitular();
        DadosDeTeste.logarComoReceptor(1L, 20L);
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptor));
        when(receptorRepository.save(receptor)).thenReturn(receptor);
        service.atualizar(20L, atualizacao("Sem glúten", null, null, null, null, null, null));
        assertThat(receptor.getRestricaoAlimentar()).isEqualTo("Sem glúten");
    }

    @Test
    @DisplayName("Atualiza observações privadas do receptor")
    void atualizarObservacao() {
        Receptor receptor = receptorTitular();
        DadosDeTeste.logarComoReceptor(1L, 20L);
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptor));
        when(receptorRepository.save(receptor)).thenReturn(receptor);
        service.atualizar(20L, atualizacao(null, null, null, "Contato pela manhã", null, null, null));
        assertThat(receptor.getObservacao()).isEqualTo("Contato pela manhã");
    }

    @Test
    @DisplayName("Atualiza os dados pessoais do titular")
    void atualizarDadosPessoais() {
        Receptor receptor = receptorTitular();
        DadosDeTeste.logarComoReceptor(1L, 20L);
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptor));
        when(receptorRepository.save(receptor)).thenReturn(receptor);
        service.atualizar(20L, new ReceptorUpdateRequestDTO("Nome atualizado", null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null));
        verify(usuarioService).atualizarParcial(receptor.getUsuario(), "Nome atualizado", null, null, null,
                null, null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Busca receptores pela presença de crianças")
    void buscarPorCriancas() {
        DadosDeTeste.logarComoAdmin(90L);
        PageRequest pageable = PageRequest.of(0, 10);
        Receptor semCriancas = receptorTitular();
        semCriancas.setPossuiCriancas(false);
        when(receptorRepository.findByPossuiCriancas(false, pageable))
                .thenReturn(new PageImpl<>(List.of(semCriancas), pageable, 1));
        assertThat(service.buscarPorPossuiCriancas("n", pageable).getContent())
                .singleElement().extracting(ReceptorResponseDTO::possuiCriancas).isNull();
        verify(receptorRepository).findByPossuiCriancas(false, pageable);
    }

    @Test
    @DisplayName("Busca receptores por restrição alimentar sem diferenciar maiúsculas")
    void buscarPorRestricao() {
        DadosDeTeste.logarComoAdmin(90L);
        PageRequest pageable = PageRequest.of(0, 10);
        when(receptorRepository.findByRestricaoAlimentarContainingIgnoreCase("Lactose", pageable))
                .thenReturn(new PageImpl<>(List.of(receptorTitular()), pageable, 1));
        assertThat(service.buscarPorRestricaoAlimentar("Lactose", pageable).getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Titular e administrador consultam os dados privados do perfil")
    void consultaTitularEAdministrador() {
        Receptor receptor = receptorTitular();
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptor));
        DadosDeTeste.logarComoReceptor(1L, 20L);
        assertThat(service.buscarPorId(20L).restricaoAlimentar()).isEqualTo("Sem lactose");
        DadosDeTeste.logarComoAdmin(90L);
        assertThat(service.buscarPorId(20L).email()).isEqualTo("usuario1@partilhae.test");
    }

    @Test
    @DisplayName("Oculta contato, endereço e dados familiares para outros usuários")
    void consultaDeTerceiro() {
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptorTitular()));
        DadosDeTeste.logarComoDoador(2L, 10L);
        ReceptorResponseDTO resposta = service.buscarPorId(20L);
        assertThat(resposta.nomeUsuario()).isEqualTo("Usuário de Teste 1");
        assertThat(resposta.email()).isNull();
        assertThat(resposta.telefone()).isNull();
        assertThat(resposta.cep()).isNull();
        assertThat(resposta.endereco()).isNull();
        assertThat(resposta.numero()).isNull();
        assertThat(resposta.quantidadePessoasFamilia()).isNull();
        assertThat(resposta.possuiCriancas()).isNull();
        assertThat(resposta.restricaoAlimentar()).isNull();
        assertThat(resposta.observacao()).isNull();
    }

    @Test
    @DisplayName("Listagem administrativa preserva contato e oculta endereço e dados familiares")
    void listagemAdministrativa() {
        DadosDeTeste.logarComoAdmin(90L);
        PageRequest pageable = PageRequest.of(0, 10);
        when(receptorRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(receptorTitular()), pageable, 1));
        ReceptorResponseDTO resposta = service.listar(pageable).getContent().getFirst();
        assertThat(resposta.email()).isEqualTo("usuario1@partilhae.test");
        assertThat(resposta.telefone()).isEqualTo("11900000000");
        assertThat(resposta.endereco()).isNull();
        assertThat(resposta.possuiCriancas()).isNull();
        assertThat(resposta.restricaoAlimentar()).isNull();
    }

    @Test
    @DisplayName("Impede atualização do perfil de outro receptor")
    void naoAtualizaOutroReceptor() {
        DadosDeTeste.logarComoReceptor(2L, 30L);
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptorTitular()));
        assertThatThrownBy(() -> service.atualizar(20L, atualizacao(null, null, null, null, null, null, null)))
                .isInstanceOf(AccessDeniedException.class);
        verify(receptorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Permite mudar ONG para pessoa física limpando nomeOrganizacao")
    void mudarOngParaPessoaFisica() {
        Receptor receptor = receptorTitular();
        receptor.setTipoBeneficiario("ONG");
        receptor.setNomeOrganizacao("ONG Teste");
        DadosDeTeste.logarComoReceptor(1L, 20L);
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptor));
        when(receptorRepository.save(receptor)).thenReturn(receptor);
        service.atualizar(20L, atualizacao(null, null, null, null, "pessoaFisica", null, null));
        assertThat(receptor.getNomeOrganizacao()).isNull();
    }

    @Test
    @DisplayName("Desativa o receptor logicamente sem excluir seu perfil")
    void desativacaoLogica() {
        Receptor receptor = receptorTitular();
        DadosDeTeste.logarComoReceptor(1L, 20L);
        when(receptorRepository.findById(20L)).thenReturn(Optional.of(receptor));
        service.desativar(20L);
        assertThat(receptor.getUsuario().getStatusConta()).isEqualTo("BLOQUEADA");
        verify(usuarioRepository).save(receptor.getUsuario());
        verify(receptorRepository, never()).delete(any());
    }

    private void prepararCadastro() {
        Usuario usuario = DadosDeTeste.usuario(1L);
        when(usuarioService.criar("Receptor Teste", "receptor@partilhae.test", "Senha@123", "52998224725",
                "11999999999", "01001000", "Rua de Teste", "10", null, "Centro", "São Paulo", "SP", "RECEPTOR"))
                .thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(receptorRepository.save(any(Receptor.class))).thenAnswer(invocacao -> {
            Receptor salvo = invocacao.getArgument(0);
            salvo.setIdReceptor(20L);
            return salvo;
        });
    }

    private static ReceptorRequestDTO pedido(String tipo, String organizacao, Boolean possuiCriancas) {
        return new ReceptorRequestDTO("Receptor Teste", "receptor@partilhae.test", "Senha@123", "52998224725",
                "11999999999", "01001000", "Rua de Teste", "10", null, "Centro", "São Paulo", "SP",
                4, possuiCriancas, "Sem lactose", "Contato à tarde", tipo, organizacao);
    }

    private static ReceptorUpdateRequestDTO atualizacao(String restricao, Integer quantidade, Boolean criancas,
                                                        String observacao, String tipo, String nome,
                                                        String nomeUsuario) {
        return new ReceptorUpdateRequestDTO(nomeUsuario, null, null, null, null, null, null, null, null,
                null, null, null, quantidade, criancas, restricao, observacao, tipo, nome);
    }

    private static Receptor receptorTitular() {
        return DadosDeTeste.receptor(20L, DadosDeTeste.usuario(1L));
    }
}
