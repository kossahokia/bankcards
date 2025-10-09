package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for assigning a new role to an existing user.
 * <p>
 * This DTO is used in administrative endpoints such as
 * {@code /api/admin/users/{id}/role} to grant specific roles
 * (e.g. {@code USER}, {@code ADMIN}) to a user account.
 * </p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code userId} – must not be {@code null}</li>
 *   <li>{@code roleName} – must not be blank</li>
 * </ul>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
public class AssignRoleRequest {

    /**
     * Identifier of the user to whom the role is being assigned.
     */
    @NotNull
    private Long userId;

    /**
     * Name of the role to assign (e.g. {@code USER}, {@code ADMIN}).
     */
    @NotBlank
    private String roleName;
}
