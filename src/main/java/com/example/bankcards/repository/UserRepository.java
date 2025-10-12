package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for performing CRUD and query operations on {@link User} entities.
 * <p>
 * Extends {@link JpaRepository}, providing full support for pagination, sorting, and
 * derived query methods. This repository is used by service layers such as
 * {@code UserService} and {@code AuthService}.
 * </p>
 *
 * <h3>Query method naming conventions:</h3>
 * <ul>
 *     <li>Queries are derived automatically from method names (Spring Data JPA).</li>
 *     <li>Case-insensitive searches are used for username-based filters.</li>
 *     <li>Pagination support is provided via {@link Pageable} arguments.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * Optional<User> userOpt = userRepository.findByUsername("john");
 * Page<User> users = userRepository.findByEnabled(true, PageRequest.of(0, 20));
 * }</pre>
 * </p>
 *
 * @see com.example.bankcards.entity.User
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see com.example.bankcards.service.UserService
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username (case-sensitive).
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the found user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds users whose username exactly matches the given value (case-insensitive).
     *
     * @param username the username to match
     * @param pageable pagination information
     * @return a page of matching users
     */
    Page<User> findByUsernameEqualsIgnoreCase(String username, Pageable pageable);

    /**
     * Finds users whose username starts with the given value (case-insensitive).
     *
     * @param username the prefix of the username
     * @param pageable pagination information
     * @return a page of matching users
     */
    Page<User> findByUsernameStartingWithIgnoreCase(String username, Pageable pageable);

    /**
     * Finds users whose username contains the given value (case-insensitive).
     *
     * @param username substring to match within username
     * @param pageable pagination information
     * @return a page of matching users
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    /**
     * Finds all users by their enabled status.
     *
     * @param enabled whether users are active
     * @param pageable pagination information
     * @return a page of users matching the enabled flag
     */
    Page<User> findByEnabled(boolean enabled, Pageable pageable);

    /**
     * Finds users by partial username match (case-insensitive) and enabled status.
     *
     * @param username substring of username
     * @param enabled whether users are active
     * @param pageable pagination information
     * @return a page of users that match both conditions
     */
    Page<User> findByUsernameContainingIgnoreCaseAndEnabled(String username, boolean enabled, Pageable pageable);

    /**
     * Finds users whose username starts with the given prefix (case-insensitive) and enabled status.
     *
     * @param username prefix of username
     * @param enabled whether users are active
     * @param pageable pagination information
     * @return a page of users that match both conditions
     */
    Page<User> findByUsernameStartingWithIgnoreCaseAndEnabled(String username, boolean enabled, Pageable pageable);

    /**
     * Finds users whose username exactly matches the given value (case-insensitive) and enabled status.
     *
     * @param username exact username to match
     * @param enabled whether users are active
     * @param pageable pagination information
     * @return a page of users that match both conditions
     */
    Page<User> findByUsernameEqualsIgnoreCaseAndEnabled(String username, boolean enabled, Pageable pageable);

}
