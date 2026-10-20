package ma.enset.tpjwtspring.config;

import ma.enset.tpjwtspring.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration centrale de Spring Security pour l'application.
 *
 * Cette classe définit :
 *   1. Comment les mots de passe sont encodés (BCrypt)
 *   2. Comment l'authentification est effectuée (AuthenticationManager)
 *   3. Quelles routes sont publiques ou protégées (SecurityFilterChain)
 *   4. Où le filtre JWT s'insère dans la chaîne de filtres
 *
 * Architecture : API REST stateless, authentifiée par token JWT.
 */
@Configuration // Indique à Spring que cette classe déclare des beans (@Bean)
public class SecurityConfig {

    /**
     * Filtre personnalisé qui lit et valide le JWT à chaque requête.
     * Il est injecté ici pour être ajouté à la chaîne de sécurité.
     */
    private final JwtFilter jwtFilter;

    /**
     * Injection de dépendance par constructeur.
     *
     * @param jwtFilter le filtre JWT (déclaré avec @Component)
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Déclare l'encodeur de mots de passe utilisé dans toute l'application.
     *
     * BCrypt est un algorithme de hachage lent et salé :
     *   - lent : rend les attaques par force brute coûteuses
     *   - salé : deux mots de passe identiques donnent deux hachés différents
     *
     * Utilisation :
     *   - À l'inscription : passwordEncoder.encode(motDePasse) avant de sauvegarder
     *   - Au login : Spring compare automatiquement le mot de passe saisi au haché en base
     *
     * @return l'encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose l'AuthenticationManager de Spring comme bean.
     *
     * Il sert dans le contrôleur de login pour vérifier email + mot de passe :
     *   authenticationManager.authenticate(
     *       new UsernamePasswordAuthenticationToken(email, password));
     *
     * En interne, il utilise CustomUserDetailsService (pour charger l'utilisateur)
     * et PasswordEncoder (pour comparer les mots de passe).
     *
     * @param configuration configuration d'authentification fournie par Spring
     * @return l'AuthenticationManager prêt à l'emploi
     * @throws Exception si la configuration est invalide
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Définit la chaîne de filtres de sécurité HTTP : c'est le cœur de la configuration.
     *
     * @param http objet de configuration de la sécurité HTTP
     * @return la chaîne de filtres construite
     * @throws Exception si la configuration est invalide
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactive la protection CSRF.
                // CSRF cible les sessions/cookies ; ici l'API est stateless et
                // le token est envoyé dans l'en-tête Authorization, donc pas nécessaire.
                .csrf(csrf -> csrf.disable())

                // STATELESS : Spring ne crée jamais de session HTTP.
                // Chaque requête doit porter son propre JWT.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Règles d'autorisation, évaluées de haut en bas (la première qui correspond gagne)
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques : inscription, login, console H2
                        .requestMatchers("/auth/**", "/h2-console/**").permitAll()
                        // Routes de l'API : un JWT valide est obligatoire
                        .requestMatchers("/api/**").authenticated()
                        // Toute autre route : publique (à remplacer par .authenticated() pour plus de sécurité)
                        .anyRequest().permitAll()
                )

                // La console H2 s'affiche dans une <iframe>.
                // Par défaut Spring bloque les iframes ; sameOrigin() les autorise
                // uniquement depuis le même domaine. À retirer en production.
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()))

                // Place le filtre JWT AVANT le filtre de login classique (formulaire).
                // Ainsi le token est lu et l'utilisateur authentifié avant les contrôles d'accès.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Construit et retourne la chaîne de filtres configurée
        return http.build();
    }
}