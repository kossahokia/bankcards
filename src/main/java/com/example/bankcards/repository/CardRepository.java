package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // блокируем строку при изменении
    Optional<Card> findWithLockById(Long id);

    // поиск карты по номеру (для переводов и уникальности)
    Optional<Card> findByCardNumber(String cardNumber);

    // карты конкретного пользователя (с постраничным выводом)
    Page<Card> findByOwner(User owner, Pageable pageable);

    // поиск карт по статусу (например, ACTIVE или BLOCKED)
    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    // проверка уникальности (на всякий случай)
    boolean existsByCardNumber(String cardNumber);
}

