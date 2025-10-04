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

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final CardRepository cardRepository;
    private final CardMaskUtil cardMaskUtil;
    private final EncryptionUtil encryptionUtil;

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
