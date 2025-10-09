package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link Card} entities.
 * <p>
 * Provides CRUD operations and custom finder methods related to card ownership,
 * status, and transactional safety. This repository is used primarily by
 * {@link com.example.bankcards.service.CardService} and
 * {@link com.example.bankcards.service.TransferService}.
 * </p>
 *
 * <h3>Concurrency and transactional notes:</h3>
 * <ul>
 *     <li>Methods annotated with {@link org.springframework.data.jpa.repository.Lock @Lock}
 *     use {@link jakarta.persistence.LockModeType#PESSIMISTIC_WRITE} to prevent concurrent
 *     modifications of the same card during transfer or status updates.</li>
 *     <li>Spring Data automatically applies the defined lock within the current transaction boundary.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * Page<Card> activeCards = cardRepository.findByStatus(CardStatus.ACTIVE, PageRequest.of(0, 10));
 * Optional<Card> lockedCard = cardRepository.findWithLockById(42L);
 * }</pre>
 * </p>
 *
 * @see com.example.bankcards.entity.Card
 * @see com.example.bankcards.entity.enums.CardStatus
 * @see com.example.bankcards.service.CardService
 * @see com.example.bankcards.service.TransferService
 * @see org.springframework.data.jpa.repository.JpaRepository
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Retrieves all cards owned by the specified user (by ID).
     *
     * @param ownerId the unique ID of the user who owns the cards
     * @param pageable pagination information
     * @return a page of cards belonging to the specified owner
     */
    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Retrieves all cards belonging to the given {@link User}.
     *
     * @param owner the user entity who owns the cards
     * @param pageable pagination information
     * @return a page of cards belonging to the provided owner
     */
    Page<Card> findByOwner(User owner, Pageable pageable);

    /**
     * Retrieves all cards owned by a user with a specific {@link CardStatus}.
     *
     * @param owner the card owner
     * @param status the desired card status
     * @param pageable pagination information
     * @return a page of matching cards
     */
    Page<Card> findByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);

    /**
     * Finds a card by its ID and owner.
     *
     * @param id the card ID
     * @param owner the owner entity
     * @return an {@link Optional} containing the card if found, otherwise empty
     */
    Optional<Card> findByIdAndOwner(Long id, User owner);

    /**
     * Retrieves all cards that have the specified status.
     *
     * @param status the {@link CardStatus} to filter by
     * @param pageable pagination information
     * @return a page of cards with the given status
     */
    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    /**
     * Retrieves a card by ID with a pessimistic write lock.
     * <p>
     * Used in transactional operations (such as balance transfers) to prevent
     * race conditions or lost updates when multiple transactions attempt to
     * modify the same card simultaneously.
     * </p>
     *
     * @param id the card ID
     * @return an {@link Optional} containing the locked card, if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findWithLockById(Long id);

    /**
     * Checks if a card with the given (encrypted) card number already exists.
     *
     * @param cardNumber the encrypted card number to check
     * @return {@code true} if a card with this number exists, otherwise {@code false}
     */
    boolean existsByCardNumber(String cardNumber);
}
