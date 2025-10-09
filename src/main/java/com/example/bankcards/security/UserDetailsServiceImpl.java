package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring Security's {@link UserDetailsService} interface,
 * responsible for loading user-specific data during authentication.
 * <p>
 * This service retrieves {@link User} entities from the database using {@link UserRepository}
 * and converts them into {@link UserDetailsImpl} objects recognized by the Spring Security framework.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *     <li>Locate a user by their username from the persistent storage.</li>
 *     <li>Throw a {@link UsernameNotFoundException} if no user is found.</li>
 *     <li>Wrap the domain {@link User} into a {@link UserDetailsImpl} instance.</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>{@code
 * UserDetails userDetails = userDetailsService.loadUserByUsername("john_doe");
 * authenticationManager.authenticate(
 *     new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password)
 * );
 * }</pre>
 *
 * @see UserDetailsImpl
 * @see com.example.bankcards.repository.UserRepository
 * @see com.example.bankcards.entity.User
 * @see org.springframework.security.core.userdetails.UserDetailsService
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their username and returns a Spring Security-compatible
     * {@link UserDetails} object.
     *
     * @param username the username of the user to retrieve
     * @return the {@link UserDetails} representing the found user
     * @throws UsernameNotFoundException if the user cannot be found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username)
                );
        return new UserDetailsImpl(user);
    }
}
