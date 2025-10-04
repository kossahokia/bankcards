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

@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskUtil cardMaskUtil;

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

    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<CardResponse> getCardsByUserId(Long id, Pageable pageable) {
        return cardRepository.findByOwnerId(id, pageable).map(this::toResponse);
    }

    public Page<CardResponse> getCardsByOwner(User owner, Pageable pageable) {
        return cardRepository.findByOwner(owner, pageable).map(this::toResponse);
    }

    public Page<CardResponse> getCardsByOwnerAndStatus(User owner, CardStatus status, Pageable pageable) {
        return cardRepository.findByOwnerAndStatus(owner, status, pageable).map(this::toResponse);
    }

    public Page<CardResponse> getCardsByStatus(CardStatus status, Pageable pageable) {
        return cardRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    public CardResponse updateCardStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findWithLockById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        card.setStatus(newStatus);
        return toResponse(cardRepository.save(card));
    }

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

    public Card findByIdAndOwner(Long id, User owner) {
        return cardRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Card not found for this user"));
    }

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
