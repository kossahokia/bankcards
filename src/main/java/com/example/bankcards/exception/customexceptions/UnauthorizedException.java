package com.example.bankcards.exception.customexceptions;

/**
 * Exception thrown when a user fails authentication or provides invalid credentials.
 * <p>
 * This exception indicates that the request lacks valid authentication credentials,
 * such as an expired, missing, or malformed JWT token.
 * </p>
 *
 * <p>
 * Typically mapped to HTTP {@code 401 UNAUTHORIZED} responses.
 * It differs from {@code ForbiddenException} (403) — this one means
 * the user is <em>not authenticated</em>, not merely lacking permissions.
 * </p>
 *
 * <p>HTTP Status: {@code 401 UNAUTHORIZED}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Creates a new {@code UnauthorizedException} with the specified message.
     *
     * @param message description of the authentication failure (e.g. "Invalid token", "User not logged in")
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
