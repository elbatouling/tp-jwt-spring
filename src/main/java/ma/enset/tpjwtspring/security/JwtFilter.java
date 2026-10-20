package ma.enset.tpjwtspring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.enset.tpjwtspring.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre exécuté une seule fois par requête HTTP.
 * Il lit le token JWT dans l'en-tête Authorization, le valide,
 * puis authentifie l'utilisateur dans le contexte de sécurité Spring.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService,
                     CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lit l'en-tête "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");

        // Pas de token : on laisse passer, Spring Security décidera (accès refusé si route protégée)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extrait le token (après "Bearer ", soit 7 caractères)
        String token = authHeader.substring(7);

        try {
            // 3. Récupère l'email contenu dans le token
            String username = jwtService.extractUsername(token);

            // 4. Si l'utilisateur n'est pas déjà authentifié dans ce contexte
            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                // 5. Vérifie la validité du token
                if (jwtService.isTokenValid(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Authentifie l'utilisateur pour cette requête
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Token invalide, expiré ou utilisateur introuvable : on n'authentifie pas
            SecurityContextHolder.clearContext();
        }

        // 7. Continue la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}