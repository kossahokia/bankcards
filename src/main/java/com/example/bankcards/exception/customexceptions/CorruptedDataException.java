package com.example.bankcards.exception.customexceptions;

/**
 * Exception thrown when data integrity or consistency issues prevent processing a request.
 * <p>
 * This exception indicates that the system detected corrupted, inconsistent,
 * or otherwise invalid data that cannot be safely processed. Typical examples include:
 * <ul>
 *     <li>Decryption or data integrity check failures</li>
 *     <li>Unexpected null or mismatched database references</li>
 *     <li>Invalid internal state caused by inconsistent entities</li>
 * </ul>
 * </p>
 *
 * <p>
 * Although this error originates server-side, it usually represents
 * a recoverable data issue rather than a full internal failure.
 * Therefore, it maps to HTTP {@code 422 UNPROCESSABLE_ENTITY}.
 * </p>
 *
 * <p>HTTP Status: {@code 422 UNPROCESSABLE_ENTITY}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public class CorruptedDataException extends RuntimeException {

    /**
     * Creates a new {@code CorruptedDataException} with a detailed message.
     *
     * @param message a description of the data inconsistency or corruption
     */
    public CorruptedDataException(String message) {
        super(message);
    }
}
