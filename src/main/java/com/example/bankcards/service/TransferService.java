package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMaskUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final CardRepository cardRepository;

    public void transferMoney(Long fromCardId, Long toCardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        // блокируем строки на уровне базы
        Card fromCard = cardRepository.findWithLockById(fromCardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта-отправитель не найдена"));
        Card toCard = cardRepository.findWithLockById(toCardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта-получатель не найдена"));

        // проверки статусов
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Карта-отправитель неактивна: " +
                    CardMaskUtil.maskCardNumber(fromCard.getCardNumber()));
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Карта-получатель неактивна: " +
                    CardMaskUtil.maskCardNumber(toCard.getCardNumber()));
        }

        // проверка баланса
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на карте: " +
                    CardMaskUtil.maskCardNumber(fromCard.getCardNumber()));
        }

        // выполняем перевод
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
