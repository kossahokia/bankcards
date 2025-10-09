package com.example.bankcards.repository;

import com.example.bankcards.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link Role} entities.
 * <p>
 * Provides basic CRUD operations and a convenience method for retrieving roles
 * by name, ignoring case sensitivity. Typically used by authentication and
 * user management services to assign or validate user roles.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * Optional<Role> adminRole = roleRepository.findByNameIgnoreCase("ADMIN");
 * }</pre>
 * </p>
 *
 * @see com.example.bankcards.entity.Role
 * @see com.example.bankcards.service.UserService
 * @see org.springframework.data.jpa.repository.JpaRepository
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its name, ignoring case sensitivity.
     *
     * @param name the name of the role to find (e.g. "USER" or "ADMIN")
     * @return an {@link Optional} containing the role if found, otherwise empty
     */
    Optional<Role> findByNameIgnoreCase(String name);
}
