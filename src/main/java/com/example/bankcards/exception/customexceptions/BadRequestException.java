package com.example.bankcards.exception.customexceptions;

/**
 * Exception thrown to indicate a client-side request error (HTTP 400 Bad Request).
 * <p>
 * Used when the request contains invalid data, violates business rules,
 * or fails validation at the service or controller level.
 * </p>
 *
 * <p>
 * This exception is typically handled by a global {@code @ControllerAdvice}
 * to return a standardized {@code ApiErrorResponse}.
 * </p>
 *
 * <p>HTTP Status: {@code 400 BAD_REQUEST}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public class BadRequestException extends RuntimeException {

    /**
     * Creates a new {@code BadRequestException} with the specified message.
     *
     * @param message detailed description of the validation or request error
     */
    public BadRequestException(String message) {
        super(message);
    }
}
