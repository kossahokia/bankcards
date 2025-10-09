package com.example.bankcards.exception.customexceptions;

/**
 * Exception thrown when a requested resource cannot be found.
 * <p>
 * Used to signal that an entity (such as {@code User} or {@code Card})
 * does not exist in the system or database.
 * </p>
 *
 * <p>
 * Typically results in an HTTP {@code 404 NOT_FOUND} response.
 * This exception is handled globally by {@code @ControllerAdvice}
 * to generate a standardized {@code ApiErrorResponse}.
 * </p>
 *
 * <p>HTTP Status: {@code 404 NOT_FOUND}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates a new {@code NotFoundException} with the specified message.
     *
     * @param message a human-readable description of the missing resource
     */
    public NotFoundException(String message) {
        super(message);
    }
}
