package br.com.partilhae.partilhae.repository;

import br.com.partilhae.partilhae.model.Beneficiario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {

    Page<Beneficiario> findByPossuiCriancas(Boolean possuiCriancas, Pageable pageable);

    Page<Beneficiario> findByRestricaoAlimentarContainingIgnoreCase(String restricaoAlimentar, Pageable pageable);

    Optional<Beneficiario> findByUsuario_Email(String email);

    Optional<Beneficiario> findByUsuario_Cpf(String cpf);

    Optional<Beneficiario> findByUsuario_IdUsuario(Long idUsuario);
}
