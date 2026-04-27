package pe.edu.upeu.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.auth_service.entity.User;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Username
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // Email
    boolean existsByEmail(String email);

    // DNI
    boolean existsByDni(String dni);

    // University code
    boolean existsByUniversityCode(String universityCode);

    // Phone
    boolean existsByPhone(String phone);
}