package br.com.partilhae.partilhae.repository;

import br.com.partilhae.partilhae.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByCpf(String cpf);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);
}