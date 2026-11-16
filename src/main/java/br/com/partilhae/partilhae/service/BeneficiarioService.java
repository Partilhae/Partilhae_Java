package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.BeneficiarioRequestDTO;
import br.com.partilhae.partilhae.dto.BeneficiarioResponseDTO;
import br.com.partilhae.partilhae.dto.BeneficiarioUpdateRequestDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.model.Beneficiario;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.BeneficiarioRepository;
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
public class BeneficiarioService {
    private final BeneficiarioRepository beneficiarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public BeneficiarioResponseDTO cadastrar(BeneficiarioRequestDTO dto) {
        String organizacao = normalizarNome(dto.nomeOrganizacao());
        validarTipoEOrganizacao(dto.tipoBeneficiario(), organizacao);
        Usuario usuario = usuarioService.criar(dto.nomeUsuario(), dto.email(), dto.senha(), dto.cpf(),
                dto.telefone(), dto.cep(), dto.endereco(), dto.numero(), dto.complemento(), dto.bairro(),
                dto.cidade(), dto.estado(), "RECEPTOR");
        usuario = usuarioRepository.save(usuario);
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setUsuario(usuario);
        beneficiario.setQuantidadePessoasFamilia(dto.quantidadePessoasFamilia());
        beneficiario.setPossuiCriancas(dto.possuiCriancas());
        beneficiario.setRestricaoAlimentar(dto.restricaoAlimentar());
        beneficiario.setObservacao(dto.observacao());
        beneficiario.setTipoBeneficiario(dto.tipoBeneficiario());
        beneficiario.setNomeOrganizacao(organizacao);
        return paraResposta(beneficiarioRepository.save(beneficiario), true, true);
    }

    @Transactional(readOnly = true)
    public Page<BeneficiarioResponseDTO> listar(Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem listar beneficiários");
        return beneficiarioRepository.findAll(pageable).map(r -> paraResposta(r, false, true));
    }

    @Transactional(readOnly = true)
    public Page<BeneficiarioResponseDTO> buscarPorPossuiCriancas(String possuiCriancas, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem consultar estes dados");
        return beneficiarioRepository.findByPossuiCriancas("S".equalsIgnoreCase(possuiCriancas), pageable)
                .map(r -> paraResposta(r, false, true));
    }

    @Transactional(readOnly = true)
    public Page<BeneficiarioResponseDTO> buscarPorRestricaoAlimentar(String termo, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem consultar estes dados");
        return beneficiarioRepository.findByRestricaoAlimentarContainingIgnoreCase(termo, pageable)
                .map(r -> paraResposta(r, false, true));
    }

    @Transactional(readOnly = true)
    public BeneficiarioResponseDTO buscarPorId(Long id) {
        Beneficiario beneficiario = buscarEntidade(id);
        validarLeitura(beneficiario);
        boolean privado = SecurityUtils.podeVerEndereco(beneficiario.getUsuario().getIdUsuario());
        return paraResposta(beneficiario, privado, privado);
    }

    public BeneficiarioResponseDTO atualizar(Long id, BeneficiarioUpdateRequestDTO dto) {
        Beneficiario beneficiario = buscarEntidade(id);
        SecurityUtils.validarPosseReceptor(beneficiario.getIdReceptor());
        Usuario usuario = beneficiario.getUsuario();
        String tipo = dto.tipoBeneficiario() == null ? beneficiario.getTipoBeneficiario() : dto.tipoBeneficiario();
        String organizacao = dto.nomeOrganizacao() == null
                ? (dto.tipoBeneficiario() == null ? beneficiario.getNomeOrganizacao() : null)
                : normalizarNome(dto.nomeOrganizacao());
        validarTipoEOrganizacao(tipo, organizacao);
        usuarioService.atualizarParcial(usuario, dto.nomeUsuario(), dto.email(), dto.senha(), dto.cpf(),
                dto.telefone(), dto.cep(), dto.endereco(), dto.numero(), dto.complemento(), dto.bairro(),
                dto.cidade(), dto.estado());
        if (dto.quantidadePessoasFamilia() != null) beneficiario.setQuantidadePessoasFamilia(dto.quantidadePessoasFamilia());
        if (dto.possuiCriancas() != null) beneficiario.setPossuiCriancas(dto.possuiCriancas());
        if (dto.restricaoAlimentar() != null) beneficiario.setRestricaoAlimentar(dto.restricaoAlimentar());
        if (dto.observacao() != null) beneficiario.setObservacao(dto.observacao());
        if (dto.tipoBeneficiario() != null) beneficiario.setTipoBeneficiario(tipo);
        if ("pessoaFisica".equals(tipo)) beneficiario.setNomeOrganizacao(null);
        else if (dto.nomeOrganizacao() != null) beneficiario.setNomeOrganizacao(organizacao);
        usuarioRepository.save(usuario);
        return paraResposta(beneficiarioRepository.save(beneficiario), true, true);
    }

    public void desativar(Long id) {
        Beneficiario beneficiario = buscarEntidade(id);
        SecurityUtils.validarPosseReceptor(beneficiario.getIdReceptor());
        Usuario usuario = beneficiario.getUsuario();
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

    private Beneficiario buscarEntidade(Long id) {
        return beneficiarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Receptor não encontrado: " + id));
    }

    private static BeneficiarioResponseDTO paraResposta(Beneficiario r, boolean dadosFamiliaresPrivados,
                                                        boolean contatoPrivado) {
        Usuario u = r.getUsuario();
        return new BeneficiarioResponseDTO(r.getIdReceptor(), u.getIdUsuario(), u.getNomeUsuario(),
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

    private void validarLeitura(Beneficiario beneficiario) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getUsuarioLogado().isDoador()
                && !SecurityUtils.getUsuarioLogado().isReceptor())
            throw new AccessDeniedException("Acesso não permitido");
    }
}
