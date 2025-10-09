package com.example.bankcards.exception.customexceptions;

/**
 * Exception thrown when a valid request cannot be processed due to logical or business constraints.
 * <p>
 * Common use cases in the BankCards API include:
 * <ul>
 *     <li>Insufficient funds for a transfer</li>
 *     <li>Attempting to use a blocked or expired card</li>
 *     <li>Invalid operation state (e.g. duplicate request, disallowed status transition)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Maps to HTTP {@code 422 UNPROCESSABLE_ENTITY}.
 * Indicates that the server understood the request but cannot process it semantically.
 * </p>
 *
 * <p>HTTP Status: {@code 422 UNPROCESSABLE_ENTITY}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public class UnprocessableEntityException extends RuntimeException {

    /**
     * Creates a new {@code UnprocessableEntityException} with a detailed message.
     *
     * @param message description of the business rule or constraint violation
     */
    public UnprocessableEntityException(String message) {
        super(message);
    }
}
