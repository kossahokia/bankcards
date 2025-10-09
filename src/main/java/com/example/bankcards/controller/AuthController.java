package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.RegisterResponse;
import com.example.bankcards.exception.ApiErrorResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller providing authentication and registration endpoints
 * for the BankCards API.
 * <p>
 * Handles user login and account creation operations.
 * Authentication is performed using JWT (JSON Web Tokens), which are
 * issued upon successful login and used for securing subsequent API requests.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Authenticate users and generate JWT tokens.</li>
 *   <li>Register new users with basic validation and role assignment.</li>
 *   <li>Return appropriate HTTP responses (e.g. {@code 200 OK}, {@code 201 CREATED}, {@code 400 BAD REQUEST}).</li>
 * </ul>
 *
 * <p>
 * All routes in this controller are publicly accessible (no prior authentication required).
 * </p>
 *
 * @see com.example.bankcards.service.AuthService
 * @see com.example.bankcards.security.JwtTokenProvider
 * @see com.example.bankcards.dto.LoginRequest
 * @see com.example.bankcards.dto.RegisterRequest
 * @see com.example.bankcards.dto.JwtResponse
 * @see com.example.bankcards.dto.RegisterResponse
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Tag(name = "Authentication", description = "Endpoints for user login and registration")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and returns a JWT token if the credentials are valid.
     * <p>
     * This endpoint validates the provided username and password.
     * If authentication succeeds, it returns a signed JWT that can be used
     * in subsequent requests via the {@code Authorization: Bearer <token>} header.
     * </p>
     *
     * @param request the login request containing username and password
     * @return a {@link JwtResponse} containing the generated JWT
     *
     * @response 200 Successful authentication, JWT returned
     * @response 400 Invalid or missing request body
     * @response 401 Invalid username or password
     */
    @Operation(summary = "Authenticate user and return JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authentication",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed: request body is invalid (e.g., missing 'username' or 'password')",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials: invalid username or password",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    /**
     * Registers a new user account.
     * <p>
     * This endpoint creates a new user with the default {@code USER} role,
     * validates input fields, and ensures that the username is unique.
     * Returns basic user information upon success.
     * </p>
     *
     * @param request the registration request containing username, password, and full name
     * @return a {@link RegisterResponse} representing the created user
     *
     * @response 201 User successfully registered
     * @response 400 Validation failed or username already taken
     */
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed: invalid request body or username already taken",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
