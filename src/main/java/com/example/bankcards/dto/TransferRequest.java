package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for performing a transfer between user's own bank cards.
 * <p>
 * This DTO is used in {@code /api/cards/transfer} endpoint to specify
 * source and destination card IDs along with the transfer amount.
 * </p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code fromCardId} – must not be {@code null}</li>
 *   <li>{@code toCardId} – must not be {@code null}</li>
 *   <li>{@code amount} – must be positive and not {@code null}</li>
 * </ul>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Data
public class TransferRequest {

    /**
     * Identifier of the card from which funds are transferred.
     */
    @NotNull
    private Long fromCardId;

    /**
     * Identifier of the card that receives the funds.
     */
    @NotNull
    private Long toCardId;

    /**
     * The amount to be transferred between the cards.
     * Must be a positive value.
     */
    @NotNull
    @Positive
    private BigDecimal amount;
}
