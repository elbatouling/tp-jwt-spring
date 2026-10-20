package ma.enset.tpjwtspring.repository;

import ma.enset.tpjwtspring.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository

public interface UserRepository extends JpaRepository<AppUser, Long> {

    // Cherche un utilisateur par email (utilisé pour le login / JWT)
    Optional<AppUser> findByEmail(String email);

    // Vérifie si un email existe déjà (utile à l'inscription)
    boolean existsByEmail(String email);
}