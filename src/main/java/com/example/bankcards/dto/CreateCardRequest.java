package com.example.bankcards.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for creating a new bank card.
 * <p>
 * This DTO is used in the {@code /api/admin/cards} endpoint to register
 * a new card for a specific user with an initial balance and expiry date.
 * </p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code cardNumber} – must contain exactly 16 digits</li>
 *   <li>{@code expiryDate} – must be a valid future date</li>
 *   <li>{@code initialBalance} – must be non-negative</li>
 *   <li>{@code ownerUsername} – must not be blank and correspond to an existing user</li>
 * </ul>
 *
 * <p>Card numbers are encrypted before persistence and are displayed
 * in masked format (e.g. <code>**** **** **** 1234</code>).</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
public class CreateCardRequest {

    /**
     * Raw 16-digit card number.
     * Will be encrypted before saving to the database.
     */
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    /**
     * Expiry date of the card.
     * Must be a date in the future.
     */
    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    /**
     * Initial balance of the new card.
     * Cannot be negative.
     */
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", message = "Balance must be non-negative")
    private BigDecimal initialBalance;

    /**
     * Username of the card owner.
     * Must correspond to an existing user account.
     */
    @NotBlank(message = "Owner username is required")
    private String ownerUsername;
}
