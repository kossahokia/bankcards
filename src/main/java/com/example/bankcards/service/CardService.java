package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.exception.customexceptions.CorruptedDataException;
import com.example.bankcards.exception.customexceptions.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardExpiryUtil;
import com.example.bankcards.util.CardMaskUtil;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing bank cards and card-related operations.
 * <p>
 * Provides methods for creating, updating, and retrieving cards,
 * as well as handling business rules related to card lifecycle,
 * encryption, masking, and status transitions.
 * </p>
 *
 * <h3>Main responsibilities:</h3>
 * <ul>
 *     <li>Create cards for specific users (with encryption and ownership binding).</li>
 *     <li>Retrieve cards by various criteria (owner, status, etc.).</li>
 *     <li>Handle card blocking requests and administrative status updates.</li>
 *     <li>Convert {@link Card} entities into secure {@link CardResponse} DTOs.</li>
 *     <li>Ensure integrity of encrypted card data (throws {@link CorruptedDataException} on mismatch).</li>
 * </ul>
 *
 * <h3>Error handling:</h3>
 * <ul>
 *     <li>{@link NotFoundException} — card or owner not found.</li>
 *     <li>{@link BadRequestException} — invalid card state or ownership violation.</li>
 *     <li>{@link CorruptedDataException} — failed decryption or corrupted database entry.</li>
 * </ul>
 *
 * <p>
 * All operations are executed inside a transactional boundary to maintain data consistency.
 * </p>
 *
 * @see CardRepository
 * @see UserRepository
 * @see EncryptionUtil
 * @see CardMaskUtil
 * @see CardExpiryUtil
 * @see CardStatus
 * @see CardResponse
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskUtil cardMaskUtil;

    /**
     * Creates a new card for a specific user (by username).
     * <p>
     * Card number is encrypted before saving, and the status is initialized as {@code ACTIVE}.
     * </p>
     *
     * @param card           the {@link Card} entity to persist
     * @param ownerUsername  the username of the card owner
     * @return {@link CardResponse} containing masked and safe card data
     * @throws NotFoundException   if owner user does not exist
     * @throws BadRequestException if card number already exists
     */
    public CardResponse createCardWithOwnerUsername(Card card, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new NotFoundException("Owner not found: " + ownerUsername));

        if (cardRepository.existsByCardNumber(encryptionUtil.encrypt(card.getCardNumber()))) {
            throw new BadRequestException("Card with this number already exists");
        }

        String encrypted = encryptionUtil.encrypt(card.getCardNumber());
        card.setCardNumber(encrypted);
        card.setOwner(owner);
        card.setStatus(CardStatus.ACTIVE);

        owner.addCard(card);
        Card saved = cardRepository.save(card);

        return toResponse(saved);
    }

    /**
     * Deletes a card by its ID.
     *
     * @param cardId the card ID
     * @throws NotFoundException if card does not exist
     */
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    /**
     * Retrieves all cards with pagination.
     *
     * @param pageable pagination configuration
     * @return a paginated list of cards
     */
    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Retrieves all cards owned by a specific user.
     *
     * @param id       user ID
     * @param pageable pagination configuration
     * @return a paginated list of card responses
     */
    public Page<CardResponse> getCardsByUserId(Long id, Pageable pageable) {
        return cardRepository.findByOwnerId(id, pageable).map(this::toResponse);
    }

    /**
     * Retrieves all cards for a specific owner.
     *
     * @param owner    the {@link User} entity
     * @param pageable pagination configuration
     * @return a paginated list of card responses
     */
    public Page<CardResponse> getCardsByOwner(User owner, Pageable pageable) {
        return cardRepository.findByOwner(owner, pageable).map(this::toResponse);
    }

    /**
     * Retrieves all cards for a specific owner filtered by status.
     *
     * @param owner    the card owner
     * @param status   the desired {@link CardStatus}
     * @param pageable pagination configuration
     * @return a paginated list of card responses
     */
    public Page<CardResponse> getCardsByOwnerAndStatus(User owner, CardStatus status, Pageable pageable) {
        return cardRepository.findByOwnerAndStatus(owner, status, pageable).map(this::toResponse);
    }

    /**
     * Retrieves all cards filtered by status.
     *
     * @param status   the desired {@link CardStatus}
     * @param pageable pagination configuration
     * @return a paginated list of card responses
     */
    public Page<CardResponse> getCardsByStatus(CardStatus status, Pageable pageable) {
        return cardRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    /**
     * Updates the status of a specific card (admin-only operation).
     *
     * @param cardId    ID of the card
     * @param newStatus new {@link CardStatus} value
     * @return updated {@link CardResponse}
     * @throws NotFoundException if card does not exist
     */
    public CardResponse updateCardStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findWithLockById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        card.setStatus(newStatus);
        return toResponse(cardRepository.save(card));
    }

    /**
     * Allows a card owner to request blocking their own active card.
     *
     * @param cardId card ID
     * @param owner  currently authenticated {@link User}
     * @return updated {@link CardResponse} with {@code BLOCK_REQUESTED} status
     * @throws NotFoundException   if card does not exist
     * @throws BadRequestException if the card is expired, blocked, or owned by another user
     */
    public CardResponse requestBlockByOwner(Long cardId, User owner) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.getOwner().equals(owner)) {
            throw new BadRequestException("You are not the owner of this card");
        }

        if (card.getStatus() != CardStatus.ACTIVE || CardExpiryUtil.isExpired(card)) {
            throw new BadRequestException("Card is not active, cannot request block");
        }

        card.setStatus(CardStatus.BLOCK_REQUESTED);
        return toResponse(cardRepository.save(card));
    }

    /**
     * Finds a card by its ID and owner.
     *
     * @param id    the card ID
     * @param owner the owner user
     * @return the found {@link Card}
     * @throws NotFoundException if card does not belong to the given user
     */
    public Card findByIdAndOwner(Long id, User owner) {
        return cardRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Card not found for this user"));
    }

    /**
     * Converts a {@link Card} entity to a safe {@link CardResponse} DTO.
     * <p>
     * Decrypts and masks the card number before returning.
     * Throws {@link CorruptedDataException} if decryption fails,
     * which usually indicates data integrity issues.
     * </p>
     *
     * @param card the source {@link Card} entity
     * @return masked and secure {@link CardResponse}
     * @throws CorruptedDataException if card data cannot be decrypted
     */
    private CardResponse toResponse(Card card) {
        String masked;
        try {
            String decrypted = encryptionUtil.decrypt(card.getCardNumber());
            masked = cardMaskUtil.maskCardNumber(decrypted);
        } catch (Exception e) {
            throw new CorruptedDataException("Corrupted card data for card id=" + card.getId());
        }

        return new CardResponse(
                card.getId(),
                masked,
                card.getOwner() != null ? card.getOwner().getUsername() : null,
                card.getExpiryDate(),
                card.getStatus().name(),
                card.getBalance()
        );
    }
}
