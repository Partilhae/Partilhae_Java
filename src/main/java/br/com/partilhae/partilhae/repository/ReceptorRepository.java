package br.com.partilhae.partilhae.repository;

import br.com.partilhae.partilhae.model.Receptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceptorRepository extends JpaRepository<Receptor, Long> {

    Page<Receptor> findByPossuiCriancas(Boolean possuiCriancas, Pageable pageable);

    Page<Receptor> findByRestricaoAlimentarContainingIgnoreCase(String restricaoAlimentar, Pageable pageable);

    Optional<Receptor> findByUsuario_Email(String email);

    Optional<Receptor> findByUsuario_Cpf(String cpf);

    Optional<Receptor> findByUsuario_IdUsuario(Long idUsuario);
}
