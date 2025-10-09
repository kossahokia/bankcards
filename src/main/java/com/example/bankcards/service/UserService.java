package com.example.bankcards.service;

import com.example.bankcards.dto.enums.UsernameMatchType;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.exception.customexceptions.NotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Service responsible for user management and administrative operations.
 * <p>
 * Provides methods for creating, retrieving, updating, deleting users, and managing their roles and statuses.
 * All operations are executed within a transactional context.
 * </p>
 *
 * <h3>Main responsibilities:</h3>
 * <ul>
 *   <li>Create new users with specific roles and encoded passwords.</li>
 *   <li>Retrieve users by ID or username.</li>
 *   <li>Filter and paginate users based on name, status, and match type.</li>
 *   <li>Assign and remove roles for existing users.</li>
 *   <li>Enable or disable user accounts.</li>
 *   <li>Delete users (with role cleanup).</li>
 * </ul>
 *
 * <h3>Error handling:</h3>
 * <ul>
 *   <li>{@link NotFoundException} — if a user or role does not exist.</li>
 *   <li>{@link BadRequestException} — if username already exists or provided data is invalid.</li>
 * </ul>
 *
 * <p>
 * This service acts as an intermediary between controllers and repositories,
 * encapsulating all business rules and data integrity checks.
 * </p>
 *
 * @see UserRepository
 * @see RoleRepository
 * @see PasswordEncoder
 * @see UsernameMatchType
 * @see NotFoundException
 * @see BadRequestException
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user with the specified attributes.
     *
     * @param username unique username
     * @param password raw password (will be encoded)
     * @param fullName user's full name
     * @param roleName name of the role to assign (must exist)
     * @param enabled  account activation flag
     * @return the persisted {@link User} entity
     * @throws BadRequestException if username already exists
     * @throws NotFoundException   if the specified role is not found
     */
    public User createUser(String username, String password, String fullName, String roleName, boolean enabled) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BadRequestException("User with this username already exists");
        }

        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .roles(roles)
                .enabled(enabled)
                .build();

        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the unique identifier of the user
     * @return the found {@link User}
     * @throws NotFoundException if the user does not exist
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the found {@link User}
     * @throws NotFoundException if no user with the specified username exists
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Retrieves a paginated list of users with optional filters by username and status.
     *
     * @param username  optional username filter (partial or exact match depending on {@link UsernameMatchType})
     * @param enabled   optional account enabled status
     * @param matchType defines how the username should be matched (EQUALS, STARTS, CONTAINS)
     * @param pageable  pagination configuration
     * @return a paginated {@link Page} of {@link User} objects
     */
    public Page<User> getAllUsers(String username, Boolean enabled, UsernameMatchType matchType, Pageable pageable) {
        if (username != null && enabled != null) {
            return switch (matchType != null ? matchType : UsernameMatchType.CONTAINS) {
                case EQUALS -> userRepository.findByUsernameEqualsIgnoreCase(username, pageable)
                        .map(u -> { u.setEnabled(u.isEnabled() && enabled); return u; });
                case STARTS -> userRepository.findByUsernameStartingWithIgnoreCase(username, pageable)
                        .map(u -> { u.setEnabled(u.isEnabled() && enabled); return u; });
                case CONTAINS -> userRepository.findByUsernameContainingIgnoreCase(username, pageable)
                        .map(u -> { u.setEnabled(u.isEnabled() && enabled); return u; });
            };
        } else if (username != null) {
            return switch (matchType != null ? matchType : UsernameMatchType.CONTAINS) {
                case EQUALS -> userRepository.findByUsernameEqualsIgnoreCase(username, pageable);
                case STARTS -> userRepository.findByUsernameStartingWithIgnoreCase(username, pageable);
                case CONTAINS -> userRepository.findByUsernameContainingIgnoreCase(username, pageable);
            };
        } else if (enabled != null) {
            return userRepository.findByEnabled(enabled, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    /**
     * Deletes a user by their ID. Removes all role associations before deletion.
     *
     * @param userId the user's ID
     * @throws NotFoundException if the user does not exist
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.getRoles().clear();
        userRepository.delete(user);
    }

    /**
     * Assigns a role to the specified user.
     *
     * @param userId   the user's ID
     * @param roleName the role name to assign
     * @return the updated {@link User}
     * @throws NotFoundException if user or role does not exist
     */
    public User assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        user.getRoles().add(role);
        return userRepository.save(user);
    }

    /**
     * Removes a role from the specified user.
     *
     * @param userId   the user's ID
     * @param roleName the role name to remove
     * @return the updated {@link User}
     * @throws NotFoundException if user or role does not exist
     */
    public User removeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    /**
     * Updates a user's enabled/disabled status.
     *
     * @param userId the user's ID
     * @param status new status value (true = enabled, false = disabled)
     * @return the updated {@link User}
     * @throws NotFoundException if user does not exist
     */
    public User updateUserEnabledStatus(Long userId, boolean status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setEnabled(status);
        return userRepository.save(user);
    }
}
