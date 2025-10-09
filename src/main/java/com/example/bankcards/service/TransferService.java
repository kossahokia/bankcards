package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.exception.customexceptions.NotFoundException;
import com.example.bankcards.exception.customexceptions.UnprocessableEntityException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardExpiryUtil;
import com.example.bankcards.util.CardMaskUtil;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service responsible for handling money transfers between a user's own cards.
 * <p>
 * Ensures all business and security constraints are met before performing the transaction:
 * <ul>
 *     <li>Both cards must belong to the same user.</li>
 *     <li>Cards must be active and not expired.</li>
 *     <li>Transfer amount must be positive and not exceed available balance.</li>
 *     <li>Card numbers are decrypted and masked only for validation and logging purposes.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This operation is executed within a single transactional boundary to ensure atomicity:
 * either both balance updates succeed, or both are rolled back in case of an error.
 * </p>
 *
 * <h3>Error handling:</h3>
 * <ul>
 *     <li>{@link BadRequestException} — invalid request (same card, wrong owner, or non-positive amount).</li>
 *     <li>{@link NotFoundException} — one of the cards does not exist.</li>
 *     <li>{@link UnprocessableEntityException} — insufficient funds, expired card, or inactive status.</li>
 * </ul>
 *
 * @see Card
 * @see User
 * @see CardStatus
 * @see CardRepository
 * @see CardExpiryUtil
 * @see CardMaskUtil
 * @see EncryptionUtil
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final CardRepository cardRepository;
    private final CardMaskUtil cardMaskUtil;
    private final EncryptionUtil encryptionUtil;

    /**
     * Transfers funds between two cards owned by the same user.
     * <p>
     * Performs a number of validation steps before committing:
     * <ul>
     *     <li>Ensures {@code amount > 0}.</li>
     *     <li>Verifies that both cards exist and belong to the same {@link User}.</li>
     *     <li>Checks both cards are {@link CardStatus#ACTIVE} and not expired.</li>
     *     <li>Validates sufficient balance on the source card.</li>
     * </ul>
     * </p>
     *
     * <p>
     * If any validation fails, an appropriate exception is thrown and the transaction is rolled back.
     * </p>
     *
     * @param user       the authenticated user performing the transfer
     * @param fromCardId ID of the source card
     * @param toCardId   ID of the destination card
     * @param amount     transfer amount (must be positive)
     *
     * @throws BadRequestException           if request parameters are invalid
     * @throws NotFoundException             if one of the cards does not exist
     * @throws UnprocessableEntityException  if card is inactive, expired, or has insufficient funds
     */
    public void transferBetweenUserCards(User user, Long fromCardId, Long toCardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be positive");
        }

        if (fromCardId.equals(toCardId)) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        Card fromCard = cardRepository.findWithLockById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Source card not found"));
        Card toCard = cardRepository.findWithLockById(toCardId)
                .orElseThrow(() -> new NotFoundException("Destination card not found"));

        if (!fromCard.getOwner().equals(user) || !toCard.getOwner().equals(user)) {
            throw new BadRequestException("Both cards must belong to the same user");
        }

        String fromMasked = cardMaskUtil.maskCardNumber(encryptionUtil.decrypt(fromCard.getCardNumber()));
        String toMasked = cardMaskUtil.maskCardNumber(encryptionUtil.decrypt(toCard.getCardNumber()));

        if (fromCard.getStatus() != CardStatus.ACTIVE || CardExpiryUtil.isExpired(fromCard)) {
            throw new UnprocessableEntityException("Source card is not active: " + fromMasked);
        }
        if (toCard.getStatus() != CardStatus.ACTIVE || CardExpiryUtil.isExpired(toCard)) {
            throw new UnprocessableEntityException("Destination card is not active: " + toMasked);
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new UnprocessableEntityException("Insufficient funds on card: " + fromMasked);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
