package br.com.partilhae.partilhae.service;

import br.com.partilhae.partilhae.dto.UsuarioRequestDTO;
import br.com.partilhae.partilhae.dto.UsuarioResponseDTO;
import br.com.partilhae.partilhae.exception.DadoDuplicadoException;
import br.com.partilhae.partilhae.exception.RecursoNaoEncontradoException;
import br.com.partilhae.partilhae.model.Usuario;
import br.com.partilhae.partilhae.repository.UsuarioRepository;
import br.com.partilhae.partilhae.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Locale;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmails;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          @Value("${APP_ADMIN_EMAILS:}") String adminEmails) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmails = adminEmails;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem listar usuários");
        return usuarioRepository.findAll(pageable).map(UsuarioService::paraResposta);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = encontrar(id);
        SecurityUtils.validarPosseUsuario(usuario.getIdUsuario());
        return paraResposta(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = encontrar(id);
        SecurityUtils.validarPosseUsuario(usuario.getIdUsuario());
        atualizarParcial(usuario, dto.nomeUsuario(), dto.email(), dto.senha(), dto.cpf(), dto.telefone(), dto.cep(),
                dto.endereco(), dto.numero(), dto.complemento(), dto.bairro(), dto.cidade(), dto.estado());
        return paraResposta(usuarioRepository.save(usuario));
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = encontrar(id);
        SecurityUtils.validarPosseUsuario(usuario.getIdUsuario());
        usuario.setStatusConta("BLOQUEADA");
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarStatus(Long id, String status) {
        if (!SecurityUtils.isAdmin()) throw new AccessDeniedException("Apenas administradores podem alterar o status");
        if (!"ATIVA".equals(status) && !"BLOQUEADA".equals(status)) {
            throw new IllegalArgumentException("Status de conta inválido");
        }
        Usuario usuario = encontrar(id);
        usuario.setStatusConta(status);
        return paraResposta(usuarioRepository.save(usuario));
    }

    @Transactional
    public Usuario criar(String nome, String email, String senha, String cpf, String telefone,
                         String cep, String endereco, String numero, String complemento,
                         String bairro, String cidade, String estado, String perfil) {
        String emailObrigatorio = obrigatorio(email, "email");
        if (!"DOADOR".equals(perfil) && !"RECEPTOR".equals(perfil)) {
            throw new IllegalArgumentException("Somente DOADOR e RECEPTOR podem ser cadastrados publicamente");
        }
        if (isEmailAdministrativo(emailObrigatorio)) {
            throw new IllegalArgumentException("E-mail reservado para administração");
        }
        if (senha == null || senha.isBlank()) throw new IllegalArgumentException("Senha é obrigatória");
        validarDadosUnicos(emailObrigatorio, cpf, null);
        Usuario usuario = new Usuario();
        usuario.setNomeUsuario(obrigatorio(nome, "nomeUsuario"));
        usuario.setEmail(emailObrigatorio);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setCpf(normalizarCpf(cpf));
        if (!cpfValido(usuario.getCpf())) throw new IllegalArgumentException("CPF inválido");
        usuario.setTelefone(normalizarTelefone(telefone));
        usuario.setCep(normalizarCep(cep));
        usuario.setEndereco(obrigatorio(endereco, "endereco"));
        usuario.setNumero(obrigatorio(numero, "numero"));
        usuario.setComplemento(normalizarOpcional(complemento));
        usuario.setBairro(obrigatorio(bairro, "bairro"));
        usuario.setCidade(obrigatorio(cidade, "cidade"));
        usuario.setEstado(obrigatorio(estado, "estado"));
        usuario.setPerfil(perfil);
        usuario.setStatusConta("ATIVA");
        return usuario;
    }

    private boolean isEmailAdministrativo(String email) {
        if (adminEmails == null || adminEmails.isBlank()) return false;
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(normalizado::equals);
    }

    @Transactional
    public void atualizarParcial(Usuario usuario, String nome, String email, String senha, String cpf,
                                 String telefone, String cep, String endereco, String numero,
                                 String complemento, String bairro, String cidade, String estado) {
        String emailNormalizado = normalizarOpcional(email);
        if (emailNormalizado != null && isEmailAdministrativo(emailNormalizado)) {
            throw new IllegalArgumentException("E-mail reservado para administração");
        }
        String cpfNormalizado = cpf == null ? null : normalizarCpf(cpf);
        validarDadosUnicos(emailNormalizado, cpfNormalizado, usuario.getIdUsuario());

        if (nome != null) usuario.setNomeUsuario(obrigatorio(nome, "nomeUsuario"));
        if (email != null) usuario.setEmail(obrigatorio(email, "email"));
        if (senha != null) {
            if (senha.isBlank()) throw new IllegalArgumentException("Senha não pode ser vazia");
            usuario.setSenha(passwordEncoder.encode(senha));
        }
        if (cpf != null) {
            if (!cpfValido(cpfNormalizado)) throw new IllegalArgumentException("CPF inválido");
            usuario.setCpf(cpfNormalizado);
        }
        if (telefone != null) usuario.setTelefone(normalizarTelefone(telefone));
        if (cep != null) usuario.setCep(normalizarCep(cep));
        if (endereco != null) usuario.setEndereco(obrigatorio(endereco, "endereco"));
        if (numero != null) usuario.setNumero(obrigatorio(numero, "numero"));
        if (complemento != null) usuario.setComplemento(normalizarOpcional(complemento));
        if (bairro != null) usuario.setBairro(obrigatorio(bairro, "bairro"));
        if (cidade != null) usuario.setCidade(obrigatorio(cidade, "cidade"));
        if (estado != null) usuario.setEstado(obrigatorio(estado, "estado"));
    }

    private void validarDadosUnicos(String email, String cpf, Long idUsuario) {
        if (email != null) usuarioRepository.findByEmailIgnoreCase(email.trim()).ifPresent(existing -> {
            if (!existing.getIdUsuario().equals(idUsuario)) throw new DadoDuplicadoException("E-mail já cadastrado");
        });
        if (cpf != null) usuarioRepository.findByCpf(normalizarCpf(cpf)).ifPresent(existing -> {
            if (!existing.getIdUsuario().equals(idUsuario)) throw new DadoDuplicadoException("CPF já cadastrado");
        });
    }

    private Usuario encontrar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    private static String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    }

    private static boolean cpfValido(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }

    private static String normalizarTelefone(String telefone) {
        String digits = telefone == null ? "" : telefone.replaceAll("[^0-9]", "");
        if (digits.length() < 10 || digits.length() > 15) throw new IllegalArgumentException("Telefone deve conter entre 10 e 15 dígitos");
        return digits;
    }

    private static String normalizarCep(String cep) {
        String digits = cep == null ? "" : cep.replaceAll("[^0-9]", "");
        if (digits.length() != 8) throw new IllegalArgumentException("CEP deve conter 8 dígitos");
        return digits;
    }

    private static String obrigatorio(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " é obrigatório");
        return value.trim();
    }

    private static String normalizarOpcional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static UsuarioResponseDTO paraResposta(Usuario user) {
        return new UsuarioResponseDTO(user.getIdUsuario(), user.getNomeUsuario(), user.getEmail(), user.getCpf(),
                user.getTelefone(), user.getCep(), user.getEndereco(), user.getNumero(), user.getComplemento(),
                user.getBairro(), user.getCidade(), user.getEstado(), user.getStatusConta());
    }
}
