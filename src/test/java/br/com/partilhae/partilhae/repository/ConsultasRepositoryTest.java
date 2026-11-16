package br.com.partilhae.partilhae.repository;

import br.com.partilhae.partilhae.model.Denuncia;
import br.com.partilhae.partilhae.model.Beneficiario;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.support.DadosDeTeste;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConsultasRepositoryTest {
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired BeneficiarioRepository beneficiarioRepository;
    @Autowired DenunciaRepository denunciaRepository;

    @Test
    @DisplayName("Busca e verifica e-mail ignorando maiúsculas e minúsculas")
    void buscaPorEmailIgnorandoMaiusculas() {
        Usuario usuario = usuarioRepository.saveAndFlush(DadosDeTeste.usuarioSemId(
                "maria@partilhae.test", "52998224725", "DOADOR"));
        assertThat(usuarioRepository.findByEmailIgnoreCase("MARIA@PARTILHAE.TEST"))
                .contains(usuario);
        assertThat(usuarioRepository.existsByEmailIgnoreCase("MARIA@PARTILHAE.TEST")).isTrue();
    }

    @Test
    @DisplayName("Busca e verifica CPF")
    void buscaPorCpf() {
        Usuario usuario = usuarioRepository.saveAndFlush(DadosDeTeste.usuarioSemId(
                "cpf@partilhae.test", "11144477735", "DOADOR"));
        assertThat(usuarioRepository.findByCpf("11144477735")).contains(usuario);
        assertThat(usuarioRepository.existsByCpf("11144477735")).isTrue();
        assertThat(usuarioRepository.existsByCpf("22222222222")).isFalse();
    }

    @Test
    @DisplayName("Consulta receptores com e sem crianças")
    void buscaPorPresencaDeCriancas() {
        Beneficiario comCriancas = novoReceptor("familia-a@partilhae.test", "52998224725", true, "Sem lactose");
        Beneficiario semCriancas = novoReceptor("familia-b@partilhae.test", "11144477735", false, "Vegetariana");
        assertThat(beneficiarioRepository.findByPossuiCriancas(true, PageRequest.of(0, 10)).getContent())
                .containsExactly(comCriancas);
        assertThat(beneficiarioRepository.findByPossuiCriancas(false, PageRequest.of(0, 10)).getContent())
                .containsExactly(semCriancas);
    }

    @Test
    @DisplayName("Busca restrição alimentar sem diferenciar maiúsculas e minúsculas")
    void buscaPorRestricaoSemDiferenciarCaixa() {
        Beneficiario receptor = novoReceptor("restricao@partilhae.test", "52998224725", false, "Sem LACTOSE");
        assertThat(beneficiarioRepository.findByRestricaoAlimentarContainingIgnoreCase("lactose",
                PageRequest.of(0, 10)).getContent()).containsExactly(receptor);
    }

    @Test
    @DisplayName("Lista denúncias do denunciante por data decrescente com paginação")
    void listaDenunciasOrdenadaEPaginada() {
        Usuario denunciante = usuarioRepository.saveAndFlush(DadosDeTeste.usuarioSemId(
                "denunciante@partilhae.test", "52998224725", "DOADOR"));
        Usuario outro = usuarioRepository.saveAndFlush(DadosDeTeste.usuarioSemId(
                "outro@partilhae.test", "11144477735", "DOADOR"));
        salvarDenuncia(denunciante, 10L, LocalDateTime.of(2026, 1, 1, 9, 0));
        Denuncia maisRecente = salvarDenuncia(denunciante, 20L, LocalDateTime.of(2026, 1, 3, 9, 0));
        salvarDenuncia(denunciante, 30L, LocalDateTime.of(2026, 1, 2, 9, 0));
        salvarDenuncia(outro, 40L, LocalDateTime.of(2026, 1, 4, 9, 0));

        Page<Denuncia> primeiraPagina = denunciaRepository
                .findByDenunciante_IdUsuarioOrderByDataDenunciaDesc(denunciante.getIdUsuario(), PageRequest.of(0, 2));
        assertThat(primeiraPagina.getContent()).extracting(Denuncia::getIdDoacao)
                .containsExactly(20L, 30L);
        assertThat(primeiraPagina.getContent().getFirst().getIdDenuncia()).isEqualTo(maisRecente.getIdDenuncia());
        assertThat(primeiraPagina.getTotalElements()).isEqualTo(3);
        assertThat(primeiraPagina.getTotalPages()).isEqualTo(2);
    }

    private Beneficiario novoReceptor(String email, String cpf, boolean possuiCriancas, String restricao) {
        Usuario usuario = usuarioRepository.saveAndFlush(DadosDeTeste.usuarioSemId(email, cpf, "RECEPTOR"));
        Beneficiario receptor = DadosDeTeste.beneficiario(null, usuario);
        receptor.setPossuiCriancas(possuiCriancas);
        receptor.setRestricaoAlimentar(restricao);
        return beneficiarioRepository.saveAndFlush(receptor);
    }

    private Denuncia salvarDenuncia(Usuario denunciante, Long idDoacao, LocalDateTime data) {
        Denuncia denuncia = DadosDeTeste.denunciaDoacao(null, denunciante, idDoacao);
        denuncia.setDataDenuncia(data);
        return denunciaRepository.saveAndFlush(denuncia);
    }
}
