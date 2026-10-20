package ma.enset.tpjwtspring.service;

import ma.enset.tpjwtspring.entity.AppUser;
import ma.enset.tpjwtspring.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service qui charge un utilisateur depuis la base de données
 * pour Spring Security.
 *
 * Spring Security l'appelle automatiquement lors de l'authentification
 * (login) afin de récupérer l'email, le mot de passe haché et le rôle.
 */
@Service // Déclare la classe comme bean Spring (injectable)
public class CustomUserDetailsService implements UserDetailsService {

    /** Accès à la table "users" via JPA. */
    private final UserRepository userRepository;

    /**
     * Injection de dépendance par constructeur.
     *
     * @param userRepository repository utilisé pour chercher les utilisateurs
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Charge un utilisateur à partir de son identifiant.
     * Ici, l'identifiant est l'email (et non un "username" classique).
     *
     * @param username l'email saisi lors du login
     * @return un objet {@link UserDetails} contenant email, mot de passe et rôle
     * @throws UsernameNotFoundException si aucun utilisateur ne correspond à cet email
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Recherche en base par email ; lève une exception si absent
        AppUser user = userRepository
                .findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur introuvable"));

        // Convertit notre entité AppUser en UserDetails (objet attendu par Spring Security)
        return User.builder()
                .username(user.getEmail())     // identifiant = email
                .password(user.getPassword())  // mot de passe déjà haché (BCrypt)
                .roles(user.getRole())         // ajoute le préfixe "ROLE_" automatiquement
                .build();
    }
}