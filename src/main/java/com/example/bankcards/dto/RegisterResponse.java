package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned after a successful user registration.
 * <p>
 * Contains basic information about the newly created account.
 * Sensitive data such as passwords are never included in this response.
 * </p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    /**
     * Unique identifier of the newly registered user.
     */
    private Long id;

    /**
     * Username of the created account.
     */
    private String username;

    /**
     * Full name of the registered user.
     */
    private String fullName;

    /**
     * The assigned role of the user (e.g. {@code USER} or {@code ADMIN}).
     */
    private String role;
}
