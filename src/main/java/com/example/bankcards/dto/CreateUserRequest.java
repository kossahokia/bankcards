package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body used by administrators to create new user accounts.
 * <p>
 * This DTO is used in the {@code /api/admin/users} endpoint for adding
 * users with specific roles and activation status.
 * </p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code username} – must not be blank; length between 3 and 50 characters</li>
 *   <li>{@code password} – must not be blank; length between 6 and 100 characters</li>
 *   <li>{@code fullName} – must not be blank; max length 100 characters</li>
 *   <li>{@code roleName} – must not be blank (e.g. {@code USER} or {@code ADMIN})</li>
 * </ul>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
public class CreateUserRequest {

    /**
     * Username of the new account.
     * Must be unique within the system.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Plain-text password for the new user.
     * Will be securely hashed before being stored in the database.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * Full name of the user for display and identification.
     */
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    /**
     * The role assigned to the user (e.g. {@code USER} or {@code ADMIN}).
     */
    @NotBlank(message = "Role is required")
    private String roleName;

    /**
     * Whether the account is active upon creation.
     * Defaults to {@code true}.
     */
    private boolean enabled = true;
}
