package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned after a successful authentication.
 * <p>
 * Contains a generated JWT access token that must be included
 * in the {@code Authorization} header of subsequent API requests.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * </pre>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    /**
     * The JSON Web Token (JWT) issued to the authenticated user.
     * Used for accessing protected API endpoints.
     */
    private String token;
}
