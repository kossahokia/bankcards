package com.example.bankcards.entity.enums;

/**
 * Enumeration representing the lifecycle status of a bank card.
 * <p>
 * Each card can be in one of several states depending on user actions
 * or system rules (e.g. expiry or manual blocking).
 * </p>
 *
 * <ul>
 *   <li>{@link #ACTIVE} — the card is active and can be used for transactions</li>
 *   <li>{@link #BLOCK_REQUESTED} — the user has requested blocking, pending admin approval</li>
 *   <li>{@link #BLOCKED} — the card has been permanently blocked and cannot be used</li>
 *   <li>{@link #EXPIRED} — the card's expiry date has passed and it is no longer valid</li>
 * </ul>
 *
 * <p>This status is stored in the {@code card.status} column.</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public enum CardStatus {

    /**
     * The card is active and can be used for operations.
     */
    ACTIVE,

    /**
     * The user has submitted a block request, awaiting admin approval.
     */
    BLOCK_REQUESTED,

    /**
     * The card is blocked and cannot be used.
     */
    BLOCKED,

    /**
     * The card has expired based on its expiry date.
     */
    EXPIRED
}
