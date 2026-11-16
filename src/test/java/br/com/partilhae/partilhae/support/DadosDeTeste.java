package br.com.partilhae.partilhae.support;

import br.com.partilhae.partilhae.model.Denuncia;
import br.com.partilhae.partilhae.model.Doador;
import br.com.partilhae.partilhae.model.Beneficiario;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.security.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

public final class DadosDeTeste {
    private DadosDeTeste() {
    }

    public static Usuario usuario(Long id) {
        return usuario(id, "usuario" + id + "@partilhae.test", "DOADOR");
    }

    public static Usuario usuario(Long id, String email, String perfil) {
        Usuario usuario = usuarioSemId(email, "52998224725", perfil);
        usuario.setIdUsuario(id);
        usuario.setNomeUsuario("Usuário de Teste " + id);
        return usuario;
    }

    public static Usuario usuarioSemId(String email, String cpf, String perfil) {
        Usuario usuario = new Usuario();
        usuario.setNomeUsuario("Usuário de Teste");
        usuario.setEmail(email);
        usuario.setSenha("$2a$10$hash-de-teste");
        usuario.setCpf(cpf);
        usuario.setTelefone("11900000000");
        usuario.setPerfil(perfil);
        usuario.setCep("01001000");
        usuario.setEndereco("Rua de Teste");
        usuario.setNumero("10");
        usuario.setComplemento("Bloco de teste");
        usuario.setBairro("Centro");
        usuario.setCidade("São Paulo");
        usuario.setEstado("SP");
        usuario.setStatusConta("ATIVA");
        return usuario;
    }

    public static Doador doador(Long id, Usuario usuario) {
        Doador doador = new Doador();
        doador.setIdDoador(id);
        doador.setUsuario(usuario);
        doador.setDisponibilidade("Finais de semana");
        doador.setObservacao("Retirada combinada");
        doador.setTipoDoador("pessoaFisica");
        return doador;
    }

    public static Beneficiario beneficiario(Long id, Usuario usuario) {
        Beneficiario receptor = new Beneficiario();
        receptor.setIdReceptor(id);
        receptor.setUsuario(usuario);
        receptor.setQuantidadePessoasFamilia(4);
        receptor.setPossuiCriancas(true);
        receptor.setRestricaoAlimentar("Sem lactose");
        receptor.setObservacao("Contato à tarde");
        receptor.setTipoBeneficiario("pessoaFisica");
        return receptor;
    }

    public static Denuncia denuncia(Long id, Usuario denunciante, Usuario denunciado) {
        Denuncia denuncia = new Denuncia();
        denuncia.setIdDenuncia(id);
        denuncia.setDenunciante(denunciante);
        denuncia.setDenunciado(denunciado);
        denuncia.setMotivo("Relato de teste");
        denuncia.setStatus("ABERTA");
        denuncia.setDataDenuncia(LocalDateTime.of(2026, 1, 15, 12, 0));
        return denuncia;
    }

    public static Denuncia denunciaDoacao(Long id, Usuario denunciante, Long idDoacao) {
        Denuncia denuncia = denuncia(id, denunciante, null);
        denuncia.setIdDoacao(idDoacao);
        return denuncia;
    }

    public static void logarComoDoador(Long idUsuario, Long idDoador) {
        logar(idUsuario, idDoador, null, "ROLE_DOADOR");
    }

    public static void logarComoReceptor(Long idUsuario, Long idReceptor) {
        logar(idUsuario, null, idReceptor, "ROLE_RECEPTOR");
    }

    public static void logarComoAdmin(Long idUsuario) {
        logar(idUsuario, null, null, "ROLE_ADMIN");
    }

    public static void deslogar() {
        SecurityContextHolder.clearContext();
    }

    private static void logar(Long idUsuario, Long idDoador, Long idReceptor, String autoridade) {
        String email = "usuario" + idUsuario + "@partilhae.test";
        UserDetailsImpl principal = new UserDetailsImpl(idUsuario, "Usuário de Teste " + idUsuario,
                email, "$2a$10$hash-de-teste", idDoador, idReceptor, true, true,
                List.of(new SimpleGrantedAuthority(autoridade)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
