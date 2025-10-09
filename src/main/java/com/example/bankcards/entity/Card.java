package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a bank card owned by a {@link User}.
 * <p>
 * Each card contains a unique number, expiry date, balance and current {@link CardStatus}.
 * Card numbers are stored in encrypted form and displayed in masked format
 * (e.g. {@code **** **** **** 1234}) when exposed through the API.
 * </p>
 *
 * <p>
 * The {@code Card} entity is linked to a specific user via {@link #owner},
 * and supports balance operations and administrative status changes.
 * </p>
 *
 * <p>Table: {@code cards}</p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    /**
     * Primary key — unique identifier of the card.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Encrypted card number.
     * <p>
     * Stored as a 16-digit value (AES-encrypted) and never exposed in plain text.
     * </p>
     */
    @Column(name = "card_number", nullable = false, unique = true, length = 20)
    private String cardNumber;

    /**
     * The user who owns this card.
     * <p>
     * Relationship: many cards can belong to one user.
     * Lazy loading is used to avoid unnecessary joins.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Expiration date of the card.
     * Once the current date exceeds this value,
     * the card status should become {@link CardStatus#EXPIRED}.
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    /**
     * Current operational status of the card.
     * Defined by the {@link CardStatus} enumeration.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    /**
     * Current available balance on the card.
     * Represented as a decimal value with two precision digits.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
}
