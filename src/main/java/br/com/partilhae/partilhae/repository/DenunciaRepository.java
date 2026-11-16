package br.com.partilhae.partilhae.repository;

import br.com.partilhae.partilhae.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    Page<Denuncia> findByDenunciante_IdUsuarioOrderByDataDenunciaDesc(Long idUsuario, Pageable pageable);
}
