package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.ReceptorRequestDTO;
import br.com.partilhae.partilhae.dto.ReceptorResponseDTO;
import br.com.partilhae.partilhae.dto.ReceptorUpdateRequestDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.model.Receptor;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.ReceptorRepository;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceptorService {
    private final ReceptorRepository receptorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public ReceptorResponseDTO cadastrar(ReceptorRequestDTO dto) {
        String organizacao = normalizarNome(dto.nomeOrganizacao());
        validarTipoEOrganizacao(dto.tipoBeneficiario(), organizacao);
        Usuario usuario = usuarioService.criar(dto.nomeUsuario(), dto.email(), dto.senha(), dto.cpf(),
                dto.telefone(), dto.cep(), dto.endereco(), dto.numero(), dto.complemento(), dto.bairro(),
                dto.cidade(), dto.estado(), "RECEPTOR");
        usuario = usuarioRepository.save(usuario);
        Receptor receptor = new Receptor();
        receptor.setUsuario(usuario);
        receptor.setQuantidadePessoasFamilia(dto.quantidadePessoasFamilia());
        receptor.setPossuiCriancas(dto.possuiCriancas());
        receptor.setRestricaoAlimentar(dto.restricaoAlimentar());
        receptor.setObservacao(dto.observacao());
        receptor.setTipoBeneficiario(dto.tipoBeneficiario());
        receptor.setNomeOrganizacao(organizacao);
        return paraResposta(receptorRepository.save(receptor), true, true);
    }

    @Transactional(readOnly = true)
    public Page<ReceptorResponseDTO> listar(Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem listar receptores");
        return receptorRepository.findAll(pageable).map(r -> paraResposta(r, false, true));
    }

    @Transactional(readOnly = true)
    public Page<ReceptorResponseDTO> buscarPorPossuiCriancas(String possuiCriancas, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem consultar estes dados");
        return receptorRepository.findByPossuiCriancas("S".equalsIgnoreCase(possuiCriancas), pageable)
                .map(r -> paraResposta(r, false, true));
    }

    @Transactional(readOnly = true)
    public Page<ReceptorResponseDTO> buscarPorRestricaoAlimentar(String termo, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem consultar estes dados");
        return receptorRepository.findByRestricaoAlimentarContainingIgnoreCase(termo, pageable)
                .map(r -> paraResposta(r, false, true));
    }

    @Transactional(readOnly = true)
    public ReceptorResponseDTO buscarPorId(Long id) {
        Receptor receptor = buscarEntidade(id);
        validarLeitura(receptor);
        boolean privado = SecurityUtils.podeVerEndereco(receptor.getUsuario().getIdUsuario());
        return paraResposta(receptor, privado, privado);
    }

    public ReceptorResponseDTO atualizar(Long id, ReceptorUpdateRequestDTO dto) {
        Receptor receptor = buscarEntidade(id);
        SecurityUtils.validarPosseReceptor(receptor.getIdReceptor());
        Usuario usuario = receptor.getUsuario();
        String tipo = dto.tipoBeneficiario() == null ? receptor.getTipoBeneficiario() : dto.tipoBeneficiario();
        String organizacao = dto.nomeOrganizacao() == null
                ? (dto.tipoBeneficiario() == null ? receptor.getNomeOrganizacao() : null)
                : normalizarNome(dto.nomeOrganizacao());
        validarTipoEOrganizacao(tipo, organizacao);
        usuarioService.atualizarParcial(usuario, dto.nomeUsuario(), dto.email(), dto.senha(), dto.cpf(),
                dto.telefone(), dto.cep(), dto.endereco(), dto.numero(), dto.complemento(), dto.bairro(),
                dto.cidade(), dto.estado());
        if (dto.quantidadePessoasFamilia() != null) receptor.setQuantidadePessoasFamilia(dto.quantidadePessoasFamilia());
        if (dto.possuiCriancas() != null) receptor.setPossuiCriancas(dto.possuiCriancas());
        if (dto.restricaoAlimentar() != null) receptor.setRestricaoAlimentar(dto.restricaoAlimentar());
        if (dto.observacao() != null) receptor.setObservacao(dto.observacao());
        if (dto.tipoBeneficiario() != null) receptor.setTipoBeneficiario(tipo);
        if ("pessoaFisica".equals(tipo)) receptor.setNomeOrganizacao(null);
        else if (dto.nomeOrganizacao() != null) receptor.setNomeOrganizacao(organizacao);
        usuarioRepository.save(usuario);
        return paraResposta(receptorRepository.save(receptor), true, true);
    }

    public void desativar(Long id) {
        Receptor receptor = buscarEntidade(id);
        SecurityUtils.validarPosseReceptor(receptor.getIdReceptor());
        Usuario usuario = receptor.getUsuario();
        usuario.setStatusConta("BLOQUEADA");
        usuarioRepository.save(usuario);
    }

    static void validarTipoEOrganizacao(String tipo, String organizacao) {
        if (!"pessoaFisica".equals(tipo) && !"ONG".equals(tipo)) throw new IllegalArgumentException("tipoBeneficiario inválido");
        if ("pessoaFisica".equals(tipo) && organizacao != null && !organizacao.isBlank())
            throw new IllegalArgumentException("nomeOrganizacao deve ser nulo para pessoaFisica");
        if ("ONG".equals(tipo) && (organizacao == null || organizacao.isBlank()))
            throw new IllegalArgumentException("nomeOrganizacao é obrigatório para ONG");
    }

    private static String normalizarNome(String nome) {
        return nome == null || nome.isBlank() ? null : nome.trim();
    }

    private Receptor buscarEntidade(Long id) {
        return receptorRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Receptor não encontrado: " + id));
    }

    private static ReceptorResponseDTO paraResposta(Receptor r, boolean dadosFamiliaresPrivados,
                                                     boolean contatoPrivado) {
        Usuario u = r.getUsuario();
        return new ReceptorResponseDTO(r.getIdReceptor(), u.getIdUsuario(), u.getNomeUsuario(),
                contatoPrivado ? u.getEmail() : null, contatoPrivado ? u.getTelefone() : null,
                dadosFamiliaresPrivados ? u.getCep() : null, dadosFamiliaresPrivados ? u.getEndereco() : null,
                dadosFamiliaresPrivados ? u.getNumero() : null, dadosFamiliaresPrivados ? u.getComplemento() : null,
                dadosFamiliaresPrivados ? u.getBairro() : null, dadosFamiliaresPrivados ? u.getCidade() : null,
                dadosFamiliaresPrivados ? u.getEstado() : null,
                dadosFamiliaresPrivados ? r.getQuantidadePessoasFamilia() : null,
                dadosFamiliaresPrivados ? r.getPossuiCriancas() : null,
                dadosFamiliaresPrivados ? r.getRestricaoAlimentar() : null,
                dadosFamiliaresPrivados ? r.getObservacao() : null, r.getTipoBeneficiario(), r.getNomeOrganizacao());
    }

    private void validarLeitura(Receptor receptor) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getUsuarioLogado().isDoador()
                && !SecurityUtils.getUsuarioLogado().isReceptor())
            throw new AccessDeniedException("Acesso não permitido");
    }
}
