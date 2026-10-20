package ma.enset.tpjwtspring.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service responsable de toute la logique JWT (JSON Web Token) :
 * génération, lecture et validation des tokens.
 *
 * Un JWT est composé de 3 parties séparées par des points :
 *   header.payload.signature
 *   - header    : algorithme de signature (ex : HS256)
 *   - payload   : les "claims" (email, date d'émission, date d'expiration)
 *   - signature : garantit que le token n'a pas été modifié
 *
 * Flux d'utilisation :
 *   1. Login réussi  -> generateToken() crée le token renvoyé au client
 *   2. Chaque requête -> le filtre JWT appelle extractUsername() puis isTokenValid()
 */
@Service
public class JwtService {

    /** Clé secrète (Base64, au moins 256 bits) lue depuis "jwt.secret". */
    @Value("${jwt.secret}")
    private String secret;

    /** Durée de vie du token en millisecondes, lue depuis "jwt.expiration". */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Construit la clé de signature HMAC à partir du secret Base64.
     *
     * @return la clé utilisée pour signer et vérifier les tokens
     * @throws io.jsonwebtoken.security.WeakKeyException si le secret fait moins de 256 bits
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un token JWT signé pour un utilisateur authentifié.
     *
     * @param userDetails l'utilisateur authentifié
     * @return le token JWT (header.payload.signature)
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait l'email de l'utilisateur à partir du token.
     *
     * @param token le JWT reçu dans l'en-tête Authorization
     * @return l'email stocké dans le claim "sub"
     * @throws io.jsonwebtoken.JwtException si le token est invalide ou falsifié
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Vérifie qu'un token est valide pour un utilisateur donné :
     * l'email correspond et le token n'est pas expiré.
     *
     * @param token       le JWT à vérifier
     * @param userDetails l'utilisateur chargé depuis la base
     * @return true si le token est valide, false sinon
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Indique si le token est expiré.
     *
     * @param token le JWT à vérifier
     * @return true si la date d'expiration est dépassée
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractAllClaims(token).getExpiration();
        return expirationDate.before(new Date());
    }

    /**
     * Parse le token, vérifie sa signature et retourne ses claims.
     *
     * @param token le JWT à analyser
     * @return les claims (payload) du token
     * @throws io.jsonwebtoken.JwtException si la signature est invalide ou le token expiré
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}