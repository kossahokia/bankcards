package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response body representing a user's bank card details.
 * <p>
 * This DTO is returned by card-related endpoints (e.g. {@code /api/cards}, {@code /api/admin/cards})
 * and contains all necessary information about the card, excluding sensitive data.
 * </p>
 *
 * <p>Card numbers are always masked for security reasons (e.g. {@code **** **** **** 1234}).</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class CardResponse {

    /**
     * Unique identifier of the card.
     */
    private Long id;

    /**
     * Masked card number in the format {@code **** **** **** 1234}.
     * Actual card number is never exposed in responses.
     */
    private String maskedCardNumber;

    /**
     * Username of the card owner.
     */
    private String ownerUsername;

    /**
     * Expiration date of the card.
     */
    private LocalDate expiryDate;

    /**
     * Current status of the card (e.g. {@code ACTIVE}, {@code BLOCKED}, {@code EXPIRED}).
     */
    private String status;

    /**
     * Current balance available on the card.
     */
    private BigDecimal balance;
}
