package com.example.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility component responsible for generating, parsing, and validating JWT tokens.
 * <p>
 * This class encapsulates all low-level JWT logic — including signing, expiration handling,
 * and claim extraction. It is used primarily by the authentication layer and request filters.
 * </p>
 *
 * <h3>Configuration</h3>
 * <ul>
 *   <li><b>Secret key:</b> provided via the {@code jwt.secret} property in {@code application.yml}.</li>
 *   <li><b>Expiration time:</b> configured through the {@code jwt.expiration} property (milliseconds).</li>
 *   <li><b>Algorithm:</b> HMAC SHA-256 ({@link SignatureAlgorithm#HS256}).</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * String token = jwtTokenProvider.generateToken("john_doe");
 * boolean valid = jwtTokenProvider.validateToken(token);
 * String username = jwtTokenProvider.getUsernameFromToken(token);
 * }</pre>
 *
 * @see io.jsonwebtoken.Jwts
 * @see com.example.bankcards.security.JwtAuthenticationFilter
 * @see com.example.bankcards.service.AuthService
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long jwtExpirationInMs;

    /**
     * Constructs a new {@code JwtTokenProvider} with the given secret and expiration settings.
     *
     * @param secret the secret key used to sign JWT tokens (must be sufficiently long)
     * @param jwtExpirationInMs the token validity period, in milliseconds
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpirationInMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    /**
     * Generates a signed JWT token for the specified username.
     *
     * @param username the username to include as the {@code sub} (subject) claim
     * @return a signed and compact JWT string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject claim) from a valid JWT token.
     *
     * @param token the JWT string
     * @return the username contained in the {@code sub} claim
     * @throws JwtException if the token cannot be parsed or is invalid
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Validates a JWT token by verifying its signature and expiration.
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
