package pe.edu.upeu.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.auth_service.entity.Privilegio;

import java.util.Optional;

@Repository
public interface PrivilegioRepository extends JpaRepository<Privilegio, Long> {
    Optional<Privilegio> findByCodigoPrivilegio(String codigoPrivilegio);
}
