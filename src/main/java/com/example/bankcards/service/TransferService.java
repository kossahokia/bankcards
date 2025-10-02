package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.customexceptions.BadRequestException;
import com.example.bankcards.exception.customexceptions.NotFoundException;
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
            throw new BadRequestException("Сумма перевода должна быть положительной");
        }

        Card fromCard = cardRepository.findWithLockById(fromCardId)
                .orElseThrow(() -> new NotFoundException("Карта-отправитель не найдена"));
        Card toCard = cardRepository.findWithLockById(toCardId)
                .orElseThrow(() -> new NotFoundException("Карта-получатель не найдена"));

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Карта-отправитель неактивна: " +
                    CardMaskUtil.maskCardNumber(fromCard.getCardNumber()));
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Карта-получатель неактивна: " +
                    CardMaskUtil.maskCardNumber(toCard.getCardNumber()));
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Недостаточно средств на карте: " +
                    CardMaskUtil.maskCardNumber(fromCard.getCardNumber()));
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
