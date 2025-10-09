package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for creating a new user account.
 * <p>
 * This DTO is used in the {@code /api/auth/register} endpoint to collect
 * user credentials and profile information for registration.
 * </p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code username} – must not be blank; length between 3 and 50 characters</li>
 *   <li>{@code password} – must not be blank; length between 6 and 100 characters</li>
 *   <li>{@code fullName} – must not be blank; max length 100 characters</li>
 * </ul>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
public class RegisterRequest {

    /**
     * Username chosen by the user.
     * Must be unique within the system.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Plain-text password provided by the user during registration.
     * Will be securely hashed before storing in the database.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * Full name of the user (for display and identification purposes).
     */
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
}
