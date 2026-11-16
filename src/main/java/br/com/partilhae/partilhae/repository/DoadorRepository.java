package br.com.partilhae.partilhae.repository;

import br.com.partilhae.partilhae.model.Doador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoadorRepository extends JpaRepository<Doador, Long> {

    Optional<Doador> findByUsuario_Email(String email);

    Optional<Doador> findByUsuario_Cpf(String cpf);

    Optional<Doador> findByUsuario_IdUsuario(Long idUsuario);
}