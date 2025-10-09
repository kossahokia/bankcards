package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for validating card expiration dates.
 * <p>
 * Provides helper methods to determine whether a given {@link com.example.bankcards.entity.Card} has expired
 * based on its {@link com.example.bankcards.entity.Card Card}'s expiry date field. This utility ensures consistent
 * date comparison logic across the application.
 * </p>
 *
 * <h3>Usage:</h3>
 * <ul>
 *     <li>Used primarily in business logic before processing payments or transfers.</li>
 *     <li>Also applied when determining whether a card can be blocked or reissued.</li>
 * </ul>
 *
 * <h3>Implementation details:</h3>
 * <ul>
 *     <li>A card is considered <strong>expired</strong> if its expiry date is before the current system date ({@link LocalDate#now()}).</li>
 *     <li>Null-safe: if expiry date is {@code null}, the method returns {@code false} (not expired).</li>
 *     <li>Throws {@link NullPointerException} if the provided card instance itself is {@code null}.</li>
 * </ul>
 *
 * <p>
 * Example:
 * <pre>{@code
 * boolean expired = CardExpiryUtil.isExpired(card);
 * if (expired) {
 *     throw new UnprocessableEntityException("Card has expired");
 * }
 * }</pre>
 * </p>
 *
 * @see Card
 * @see java.time.LocalDate
 * @see com.example.bankcards.service.TransferService
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public final class CardExpiryUtil {

    /** Private constructor to prevent instantiation (static utility class). */
    private CardExpiryUtil() {
    }

    /**
     * Determines whether the given card has expired.
     *
     * @param card the {@link Card} entity to check (must not be {@code null})
     * @return {@code true} if the card’s expiry date is before the current date, otherwise {@code false}
     * @throws NullPointerException if the provided card is {@code null}
     */
    public static boolean isExpired(@NotNull Card card) {
        Objects.requireNonNull(card, "Card cannot be null");
        return card.getExpiryDate() != null
                && card.getExpiryDate().isBefore(LocalDate.now());
    }
}
