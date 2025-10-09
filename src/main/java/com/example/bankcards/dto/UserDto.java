package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing basic user information.
 * <p>
 * Used for transferring user data between the backend and the client,
 * without exposing sensitive internal details (such as passwords or roles).
 * </p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class UserDto {

    /**
     * Unique identifier of the user.
     */
    private Long id;

    /**
     * Username of the user (used for authentication).
     */
    private String username;

    /**
     * Full name of the user (first and last name combined).
     */
    private String fullName;

    /**
     * Indicates whether the user account is active or disabled.
     */
    private boolean enabled;
}
