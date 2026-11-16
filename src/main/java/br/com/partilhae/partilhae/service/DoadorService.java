package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.DoadorRequestDTO;
import br.com.partilhae.partilhae.dto.DoadorResponseDTO;
import br.com.partilhae.partilhae.dto.DoadorUpdateRequestDTO;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.model.Doador;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.DoadorRepository;
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
public class DoadorService {
    private final DoadorRepository doadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public DoadorResponseDTO cadastrar(DoadorRequestDTO dto) {
        String nome = normalizarNome(dto.nomeEstabelecimento());
        validarTipoEEstabelecimento(dto.tipoDoador(), nome);
        Usuario usuario = usuarioService.criar(dto.nomeUsuario(), dto.email(), dto.senha(), dto.cpf(),
                dto.telefone(), dto.cep(), dto.endereco(), dto.numero(), dto.complemento(), dto.bairro(),
                dto.cidade(), dto.estado(), "DOADOR");
        usuario = usuarioRepository.save(usuario);
        Doador doador = new Doador();
        doador.setUsuario(usuario);
        doador.setDisponibilidade(dto.disponibilidade());
        doador.setObservacao(dto.observacao());
        doador.setTipoDoador(dto.tipoDoador());
        doador.setNomeEstabelecimento(nome);
        return paraResposta(doadorRepository.save(doador));
    }

    @Transactional(readOnly = true)
    public Page<DoadorResponseDTO> listar(Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem listar doadores");
        return doadorRepository.findAll(pageable).map(d -> paraResposta(d, false, true));
    }

    @Transactional(readOnly = true)
    public DoadorResponseDTO buscarPorId(Long id) {
        Doador doador = buscarEntidade(id);
        validarLeitura(doador);
        boolean privado = SecurityUtils.podeVerEndereco(doador.getUsuario().getIdUsuario());
        return paraResposta(doador, privado, privado);
    }

    public DoadorResponseDTO atualizar(Long id, DoadorUpdateRequestDTO dto) {
        Doador doador = buscarEntidade(id);
        SecurityUtils.validarPosseDoador(doador.getIdDoador());
        Usuario usuario = doador.getUsuario();
        String tipo = dto.tipoDoador() == null ? doador.getTipoDoador() : dto.tipoDoador();
        String nome = dto.nomeEstabelecimento() == null
                ? (dto.tipoDoador() == null ? doador.getNomeEstabelecimento() : null)
                : normalizarNome(dto.nomeEstabelecimento());
        validarTipoEEstabelecimento(tipo, nome);
        usuarioService.atualizarParcial(usuario, dto.nomeUsuario(), dto.email(), dto.senha(), dto.cpf(),
                dto.telefone(), dto.cep(), dto.endereco(), dto.numero(), dto.complemento(), dto.bairro(),
                dto.cidade(), dto.estado());
        if (dto.disponibilidade() != null) doador.setDisponibilidade(dto.disponibilidade());
        if (dto.observacao() != null) doador.setObservacao(dto.observacao());
        if (dto.tipoDoador() != null) doador.setTipoDoador(tipo);
        if ("pessoaFisica".equals(tipo)) doador.setNomeEstabelecimento(null);
        else if (dto.nomeEstabelecimento() != null) doador.setNomeEstabelecimento(nome);
        usuarioRepository.save(usuario);
        return paraResposta(doadorRepository.save(doador));
    }

    public void desativar(Long id) {
        Doador doador = buscarEntidade(id);
        SecurityUtils.validarPosseDoador(doador.getIdDoador());
        Usuario usuario = doador.getUsuario();
        usuario.setStatusConta("BLOQUEADA");
        usuarioRepository.save(usuario);
    }

    static void validarTipoEEstabelecimento(String tipo, String nome) {
        if (!"pessoaFisica".equals(tipo) && !"estabelecimento".equals(tipo) && !"projetoSocial".equals(tipo))
            throw new IllegalArgumentException("tipoDoador inválido");
        if ("pessoaFisica".equals(tipo) && nome != null && !nome.isBlank())
            throw new IllegalArgumentException("nomeEstabelecimento deve ser nulo para pessoaFisica");
        if (!"pessoaFisica".equals(tipo) && (nome == null || nome.isBlank()))
            throw new IllegalArgumentException("nomeEstabelecimento é obrigatório para este tipo de doador");
    }

    private static String normalizarNome(String nome) {
        return nome == null || nome.isBlank() ? null : nome.trim();
    }

    private Doador buscarEntidade(Long id) {
        return doadorRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Doador não encontrado: " + id));
    }

    private void validarLeitura(Doador doador) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getUsuarioLogado().isReceptor()
                && !SecurityUtils.getUsuarioLogado().isDoador())
            throw new AccessDeniedException("Acesso não permitido");
    }

    private static DoadorResponseDTO paraResposta(Doador doador) { return paraResposta(doador, true, true); }

    private static DoadorResponseDTO paraResposta(Doador d, boolean enderecoPrivado, boolean contatoPrivado) {
        Usuario u = d.getUsuario();
        return new DoadorResponseDTO(d.getIdDoador(), u.getIdUsuario(), u.getNomeUsuario(),
                contatoPrivado ? u.getEmail() : null, contatoPrivado ? u.getTelefone() : null,
                enderecoPrivado ? u.getCep() : null, enderecoPrivado ? u.getEndereco() : null,
                enderecoPrivado ? u.getNumero() : null, enderecoPrivado ? u.getComplemento() : null,
                enderecoPrivado ? u.getBairro() : null, enderecoPrivado ? u.getCidade() : null,
                enderecoPrivado ? u.getEstado() : null, d.getDisponibilidade(), d.getObservacao(),
                d.getTipoDoador(), d.getNomeEstabelecimento());
    }
}
