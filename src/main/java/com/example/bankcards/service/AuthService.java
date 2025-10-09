package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.RegisterResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service responsible for user authentication and registration logic.
 * <p>
 * Provides two main operations:
 * <ul>
 *     <li><b>Authentication</b> — validates user credentials and issues a JWT token.</li>
 *     <li><b>Registration</b> — creates a new user account with encoded password and default {@code USER} role.</li>
 * </ul>
 * </p>
 *
 * <h3>Authentication Flow</h3>
 * <ol>
 *     <li>Receives username and password.</li>
 *     <li>Delegates verification to {@link AuthenticationManager}.</li>
 *     <li>If valid, generates a JWT token via {@link JwtTokenProvider}.</li>
 *     <li>If invalid, throws {@link BadCredentialsException} (HTTP 401).</li>
 * </ol>
 *
 * <h3>Registration Flow</h3>
 * <ol>
 *     <li>Validates that username and password are not blank.</li>
 *     <li>Ensures username is unique in the system.</li>
 *     <li>Assigns the default role {@code USER} (fetched from {@link RoleRepository}).</li>
 *     <li>Encodes the password using {@link PasswordEncoder}.</li>
 *     <li>Persists a new {@link User} entity and returns {@link RegisterResponse} DTO.</li>
 * </ol>
 *
 * <p>
 * Validation and business rule violations are handled using {@link BadRequestException},
 * which results in HTTP 400 responses.
 * </p>
 *
 * @see AuthenticationManager
 * @see JwtTokenProvider
 * @see UserRepository
 * @see RoleRepository
 * @see PasswordEncoder
 * @see BadCredentialsException
 * @see BadRequestException
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user with the provided username and password.
     *
     * @param username user's login identifier
     * @param password raw (unencoded) password
     * @return a newly generated JWT token if authentication is successful
     * @throws BadCredentialsException if the credentials are invalid
     */
    public String authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return jwtTokenProvider.generateToken(userDetails.getUsername());
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password", e);
        }
    }

    /**
     * Registers a new user in the system.
     *
     * @param request registration details including username, password, and full name
     * @return {@link RegisterResponse} containing created user information
     * @throws BadRequestException if username/password are blank, username already exists,
     *                             or default role is missing
     */
    public RegisterResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Username and password must not be blank");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username is already taken");
        }

        Role userRole = roleRepository.findByNameIgnoreCase("USER")
                .orElseThrow(() -> new BadRequestException("Default role USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(Collections.singleton(userRole))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                userRole.getName()
        );
    }
}
