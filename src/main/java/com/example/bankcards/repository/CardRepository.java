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

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Card> findByOwner(User owner, Pageable pageable);

    Page<Card> findByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);

    Optional<Card> findByIdAndOwner(Long id, User owner);

    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findWithLockById(Long id);

    boolean existsByCardNumber(String cardNumber);
}
