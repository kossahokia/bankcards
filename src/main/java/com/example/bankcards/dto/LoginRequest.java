package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for user authentication.
 * <p>
 * This DTO is used in the {@code /api/auth/login} endpoint
 * to submit user credentials for JWT-based authentication.
 * </p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code username} – must not be blank</li>
 *   <li>{@code password} – must not be blank</li>
 * </ul>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
public class LoginRequest {

    /**
     * Username of the account attempting to log in.
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Plain-text password of the user.
     * Will be validated and not stored directly.
     */
    @NotBlank(message = "Password is required")
    private String password;
}
