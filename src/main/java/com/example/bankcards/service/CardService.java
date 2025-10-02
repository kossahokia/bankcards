package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.customexceptions.NotFoundException;
import com.example.bankcards.repository.CardRepository;
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

    public Card createCard(Card card) {
        return cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Карта не найдена");
        }
        cardRepository.deleteById(cardId);
    }

    public Page<Card> getCardsByOwner(User owner, Pageable pageable) {
        return cardRepository.findByOwner(owner, pageable);
    }

    public Page<Card> getCardsByStatus(CardStatus status, Pageable pageable) {
        return cardRepository.findByStatus(status, pageable);
    }

    public Card updateCardStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findWithLockById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));

        card.setStatus(newStatus);
        return cardRepository.save(card);
    }
}
