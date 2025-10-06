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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMaskUtil cardMaskUtil;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private TransferService transferService;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("bob");

        lenient().when(encryptionUtil.decrypt(any())).thenReturn("4111111111111111");
        lenient().when(cardMaskUtil.maskCardNumber(any())).thenReturn("**** **** **** 1111");
    }

    private Card createCard(Long id, User owner, CardStatus status, BigDecimal balance, LocalDate expiry) {
        Card card = new Card();
        card.setId(id);
        card.setOwner(owner);
        card.setStatus(status);
        card.setCardNumber("ENCRYPTED");
        card.setBalance(balance);
        card.setExpiryDate(expiry);
        return card;
    }

    @Test
    @DisplayName("✅ Successful transfer — both cards ACTIVE, not expired, sufficient balance")
    void transfer_Success() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("1000.00"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("50.00"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(false);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("200.00"));

            assertThat(from.getBalance()).isEqualByComparingTo("800.00");
            assertThat(to.getBalance()).isEqualByComparingTo("250.00");

            verify(cardRepository, times(1)).save(from);
            verify(cardRepository, times(1)).save(to);
        }
    }


    @Test
    @DisplayName("❌ Amount ≤ 0 should throw BadRequestException")
    void transfer_AmountNonPositive_ThrowsBadRequest() {
        assertThatThrownBy(() ->
                transferService.transferBetweenUserCards(user, 1L, 2L, BigDecimal.ZERO)
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Transfer amount must be positive");
    }

    @Test
    @DisplayName("❌ Same card IDs should throw BadRequestException")
    void transfer_SameCardIds_ThrowsBadRequest() {
        assertThatThrownBy(() ->
                transferService.transferBetweenUserCards(user, 1L, 1L, new BigDecimal("100"))
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transfer to the same card");
    }

    @Test
    @DisplayName("❌ Source card not found should throw NotFoundException")
    void transfer_SourceCardNotFound_ThrowsNotFound() {
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
        ).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Source card not found");
    }

    @Test
    @DisplayName("❌ Destination card not found should throw NotFoundException")
    void transfer_DestinationCardNotFound_ThrowsNotFound() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now().plusDays(10));
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
        ).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Destination card not found");
    }

    @Test
    @DisplayName("❌ Cards belonging to different users should throw BadRequestException")
    void transfer_DifferentUsers_ThrowsBadRequest() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now().plusDays(10));
        Card to = createCard(2L, otherUser, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now().plusDays(10));

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        assertThatThrownBy(() ->
                transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("50"))
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Both cards must belong to the same user");
    }

    @Test
    @DisplayName("❌ Source card not ACTIVE should throw UnprocessableEntityException")
    void transfer_SourceNotActive_ThrowsUnprocessable() {
        Card from = createCard(1L, user, CardStatus.BLOCKED, new BigDecimal("500"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(false);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            assertThatThrownBy(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
            ).isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Source card is not active");
        }
    }


    @Test
    @DisplayName("❌ Destination card not ACTIVE should throw UnprocessableEntityException")
    void transfer_DestinationNotActive_ThrowsUnprocessable() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("500"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.BLOCKED, new BigDecimal("100"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(false);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            assertThatThrownBy(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
            ).isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Destination card is not active");
        }
    }


    @Test
    @DisplayName("❌ Expired source card should throw UnprocessableEntityException")
    void transfer_ExpiredCard_ThrowsUnprocessable() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("500"), LocalDate.now().plusDays(30));
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now().plusDays(30));

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(true);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            assertThatThrownBy(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
            ).isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Source card is not active");
        }
    }


    @Test
    @DisplayName("❌ Insufficient funds should throw UnprocessableEntityException")
    void transfer_InsufficientFunds_ThrowsUnprocessable() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("50"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(false);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            assertThatThrownBy(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
            ).isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Insufficient funds");
        }
    }


    @Test
    @DisplayName("✅ Source card ACTIVE and not expired should not trigger UnprocessableEntityException")
    void transfer_SourceCardActive_NotExpired_NoException() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("500"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(false);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            assertThatCode(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("50"))
            ).doesNotThrowAnyException();
        }
    }


    @Test
    @DisplayName("✅ Destination card ACTIVE and not expired should not trigger UnprocessableEntityException")
    void transfer_DestinationCardActive_NotExpired_NoException() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("500"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(false);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            assertThatCode(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("50"))
            ).doesNotThrowAnyException();
        }
    }


    @Test
    @DisplayName("❌ Source card belongs to another user should throw BadRequestException")
    void transfer_SourceCardBelongsToOtherUser_ThrowsBadRequest() {
        Card from = createCard(1L, otherUser, CardStatus.ACTIVE, new BigDecimal("500"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("500"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        assertThatThrownBy(() ->
                transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Both cards must belong to the same user");
    }



    @Test
    @DisplayName("❌ Destination card expired should throw UnprocessableEntityException")
    void transfer_DestinationCardExpired_ThrowsUnprocessable() {
        Card from = createCard(1L, user, CardStatus.ACTIVE, new BigDecimal("500"), LocalDate.now().plusDays(30));
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now().plusDays(30));

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(false);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(true);

            assertThatThrownBy(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
            ).isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Destination card is not active");
        }
    }


    @Test
    @DisplayName("❌ Source card both BLOCKED and expired should throw UnprocessableEntityException")
    void transfer_SourceBlockedAndExpired_ThrowsUnprocessable() {
        Card from = createCard(1L, user, CardStatus.BLOCKED, new BigDecimal("500"), LocalDate.now());
        Card to = createCard(2L, user, CardStatus.ACTIVE, new BigDecimal("100"), LocalDate.now());

        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(2L)).thenReturn(Optional.of(to));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(from)).thenReturn(true);
            mocked.when(() -> CardExpiryUtil.isExpired(to)).thenReturn(false);

            assertThatThrownBy(() ->
                    transferService.transferBetweenUserCards(user, 1L, 2L, new BigDecimal("100"))
            ).isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Source card is not active");
        }
    }
}
