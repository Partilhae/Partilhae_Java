package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.DenunciaRequestDTO;
import br.com.partilhae.partilhae.dto.DenunciaResponseDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.model.Denuncia;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.DenunciaRepository;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.security.SecurityUtils;
import br.com.partilhae.partilhae.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DenunciaService {
    private final DenunciaRepository denunciaRepository;
    private final UsuarioRepository usuarioRepository;

    public DenunciaResponseDTO cadastrar(DenunciaRequestDTO dto) {
        if (dto == null || !dto.isAlvoUnico()) {
            throw new IllegalArgumentException("A denúncia deve possuir exatamente um alvo");
        }
        UserDetailsImpl principal = SecurityUtils.getUsuarioLogado();
        Usuario denunciante = usuario(principal.getIdUsuario());
        if (!"ATIVA".equalsIgnoreCase(denunciante.getStatusConta()))
            throw new AccessDeniedException("Conta bloqueada não pode registrar denúncias");

        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(denunciante);
        if (dto.idUsuarioDenunciado() != null) {
            Usuario denunciado = usuario(dto.idUsuarioDenunciado());
            if (denunciante.getIdUsuario().equals(denunciado.getIdUsuario()))
                throw new IllegalArgumentException("Não é permitido denunciar a própria conta");
            denuncia.setDenunciado(denunciado);
        } else if (dto.idDoacao() != null) {
            denuncia.setIdDoacao(dto.idDoacao());
        } else if (dto.idConversa() != null) {
            denuncia.setIdConversa(dto.idConversa());
        } else if (dto.idSolicitacao() != null) {
            denuncia.setIdSolicitacao(dto.idSolicitacao());
        } else {
            throw new IllegalArgumentException("A denúncia deve possuir exatamente um alvo");
        }

        denuncia.setMotivo(dto.motivo().trim());
        denuncia.setStatus("ABERTA");
        Denuncia saved = denunciaRepository.save(denuncia);
        return paraResposta(saved);
    }

    @Transactional(readOnly = true)
    public Page<DenunciaResponseDTO> listar(Pageable pageable) {
        exigirAdmin();
        return denunciaRepository.findAll(pageable).map(DenunciaService::paraResposta);
    }

    @Transactional(readOnly = true)
    public DenunciaResponseDTO buscar(Long id) {
        Denuncia denuncia = encontrar(id);
        validarAcesso(denuncia);
        return paraResposta(denuncia);
    }

    @Transactional(readOnly = true)
    public Page<DenunciaResponseDTO> listarPorUsuario(Long idUsuario, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) SecurityUtils.validarPosseUsuario(idUsuario);
        return denunciaRepository.findByDenunciante_IdUsuarioOrderByDataDenunciaDesc(idUsuario, pageable)
                .map(DenunciaService::paraResposta);
    }

    public DenunciaResponseDTO atualizarStatus(Long id, String status) {
        exigirAdmin();
        if (status == null || !java.util.Set.of("ABERTA", "EM_ANALISE", "RESOLVIDA", "REJEITADA").contains(status))
            throw new IllegalArgumentException("Status de denúncia inválido");
        Denuncia denuncia = encontrar(id);
        denuncia.setStatus(status);
        return paraResposta(denunciaRepository.save(denuncia));
    }

    public DenunciaResponseDTO atualizarResolucao(Long id, String resolucao) {
        exigirAdmin();
        if (resolucao == null || resolucao.isBlank()) {
            throw new IllegalArgumentException("A resolução é obrigatória");
        }
        Denuncia denuncia = encontrar(id);
        denuncia.setResolucao(resolucao.trim());
        denuncia.setStatus("RESOLVIDA");
        return paraResposta(denunciaRepository.save(denuncia));
    }

    private void validarAcesso(Denuncia denuncia) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getUsuarioLogado().getIdUsuario().equals(denuncia.getDenunciante().getIdUsuario()))
            throw new AccessDeniedException("Você não tem permissão para consultar esta denúncia");
    }

    private void exigirAdmin() {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem consultar ou alterar denúncias");
    }

    private Denuncia encontrar(Long id) {
        return denunciaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Denúncia não encontrada: " + id));
    }

    private Usuario usuario(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    private static DenunciaResponseDTO paraResposta(Denuncia d) {
        return new DenunciaResponseDTO(d.getIdDenuncia(), d.getDenunciante().getIdUsuario(),
                d.getDenunciado() == null ? null : d.getDenunciado().getIdUsuario(), d.getIdConversa(),
                d.getIdSolicitacao(), d.getIdDoacao(), d.getMotivo(), d.getStatus(), d.getDataDenuncia(), d.getResolucao());
    }
}
