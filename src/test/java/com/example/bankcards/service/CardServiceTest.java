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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CardService}.
 * <p>
 * Covers core business logic for card management, including:
 * </p>
 * <ul>
 *   <li>Creating cards with encrypted numbers and validating duplicates.</li>
 *   <li>Retrieving cards (by owner, user ID, or status) and mapping to {@link CardResponse}.</li>
 *   <li>Handling deletion, blocking requests, and status updates with pessimistic locking.</li>
 *   <li>Proper exception propagation ({@link BadRequestException}, {@link NotFoundException}, {@link CorruptedDataException}).</li>
 * </ul>
 *
 * <h3>Testing strategy:</h3>
 * <ul>
 *   <li>Pure unit tests — Spring context not loaded.</li>
 *   <li>Dependencies mocked via Mockito.</li>
 *   <li>Static methods ({@link CardExpiryUtil#isExpired(Card)}) mocked using {@code mockStatic}.</li>
 *   <li>Assertions performed via AssertJ fluent API.</li>
 * </ul>
 *
 * @see com.example.bankcards.repository.CardRepository
 * @see com.example.bankcards.repository.UserRepository
 * @see com.example.bankcards.util.EncryptionUtil
 * @see com.example.bankcards.util.CardMaskUtil
 * @see com.example.bankcards.util.CardExpiryUtil
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private EncryptionUtil encryptionUtil;
    @Mock private CardMaskUtil cardMaskUtil;

    @InjectMocks private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");

        card = new Card();
        card.setId(100L);
        card.setCardNumber("4111111111111111");
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryDate(LocalDate.now().plusYears(1));
        card.setBalance(BigDecimal.valueOf(1000));
    }

    // ---------- createCardWithOwnerUsername ----------

    @Test
    @DisplayName("✅ createCardWithOwnerUsername success")
    void createCard_Success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt("4111111111111111")).thenReturn("enc");
        when(cardRepository.existsByCardNumber("enc")).thenReturn(false);
        when(encryptionUtil.decrypt("enc")).thenReturn("4111111111111111");
        when(cardMaskUtil.maskCardNumber("4111111111111111")).thenReturn("4111 **** **** 1111");
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        CardResponse resp = cardService.createCardWithOwnerUsername(card, "alice");

        assertThat(resp.getMaskedCardNumber()).contains("****");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("❌ createCardWithOwnerUsername throws when owner not found")
    void createCard_OwnerNotFound() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCardWithOwnerUsername(card, "bob"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner not found");
    }

    @Test
    @DisplayName("❌ createCardWithOwnerUsername throws when card already exists")
    void createCard_AlreadyExists() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt("4111111111111111")).thenReturn("enc");
        when(cardRepository.existsByCardNumber("enc")).thenReturn(true);

        assertThatThrownBy(() -> cardService.createCardWithOwnerUsername(card, "alice"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    // ---------- deleteCard ----------

    @Test
    @DisplayName("✅ deleteCard success")
    void deleteCard_Success() {
        when(cardRepository.existsById(100L)).thenReturn(true);

        cardService.deleteCard(100L);

        verify(cardRepository).deleteById(100L);
    }

    @Test
    @DisplayName("❌ deleteCard throws when not found")
    void deleteCard_NotFound() {
        when(cardRepository.existsById(100L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.deleteCard(100L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Card not found");
    }

    // ---------- getAllCards / getCardsBy... ----------

    @Test
    @DisplayName("✅ getAllCards returns mapped page")
    void getAllCards_Success() {
        when(cardRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(encryptionUtil.decrypt(anyString())).thenReturn("4111111111111111");
        when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("masked");

        Page<CardResponse> page = cardService.getAllCards(PageRequest.of(0, 1));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getMaskedCardNumber()).isEqualTo("masked");
    }

    @Test
    @DisplayName("✅ getCardsByUserId")
    void getCardsByUserId_Success() {
        when(cardRepository.findByOwnerId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(encryptionUtil.decrypt(anyString())).thenReturn("num");
        when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("mask");

        Page<CardResponse> page = cardService.getCardsByUserId(1L, Pageable.unpaged());

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getCardsByOwner")
    void getCardsByOwner_Success() {
        when(cardRepository.findByOwner(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(encryptionUtil.decrypt(anyString())).thenReturn("num");
        when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("mask");

        assertThat(cardService.getCardsByOwner(user, Pageable.unpaged()).getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getCardsByOwnerAndStatus")
    void getCardsByOwnerAndStatus_Success() {
        when(cardRepository.findByOwnerAndStatus(eq(user), eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(encryptionUtil.decrypt(anyString())).thenReturn("num");
        when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("mask");

        assertThat(cardService.getCardsByOwnerAndStatus(user, CardStatus.ACTIVE, Pageable.unpaged())
                .getContent()).hasSize(1);
    }

    @Test
    @DisplayName("✅ getCardsByStatus")
    void getCardsByStatus_Success() {
        when(cardRepository.findByStatus(eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(encryptionUtil.decrypt(anyString())).thenReturn("num");
        when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("mask");

        assertThat(cardService.getCardsByStatus(CardStatus.ACTIVE, Pageable.unpaged())
                .getContent()).hasSize(1);
    }

    // ---------- updateCardStatus ----------

    @Test
    @DisplayName("✅ updateCardStatus success")
    void updateCardStatus_Success() {
        when(cardRepository.findWithLockById(100L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(encryptionUtil.decrypt(anyString())).thenReturn("num");
        when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("mask");

        CardResponse resp = cardService.updateCardStatus(100L, CardStatus.BLOCKED);
        assertThat(resp.getStatus()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("❌ updateCardStatus throws when not found")
    void updateCardStatus_NotFound() {
        when(cardRepository.findWithLockById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.updateCardStatus(100L, CardStatus.ACTIVE))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------- requestBlockByOwner ----------

    @Test
    @DisplayName("✅ requestBlockByOwner success")
    void requestBlockByOwner_Success() {
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));
        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(card)).thenReturn(false);
            when(encryptionUtil.decrypt(anyString())).thenReturn("num");
            when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("mask");
            when(cardRepository.save(any(Card.class))).thenReturn(card);

            CardResponse resp = cardService.requestBlockByOwner(100L, user);
            assertThat(resp.getStatus()).isEqualTo(CardStatus.BLOCK_REQUESTED.name());
        }
    }

    @Test
    @DisplayName("❌ requestBlockByOwner throws when not found")
    void requestBlockByOwner_NotFound() {
        when(cardRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.requestBlockByOwner(100L, user))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("❌ requestBlockByOwner throws when not owner")
    void requestBlockByOwner_NotOwner() {
        User another = new User();
        another.setId(2L);
        card.setOwner(user);
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.requestBlockByOwner(100L, another))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not the owner");
    }

    @Test
    @DisplayName("❌ requestBlockByOwner throws when card not active or expired")
    void requestBlockByOwner_NotActiveOrExpired() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(card)).thenReturn(false);

            assertThatThrownBy(() -> cardService.requestBlockByOwner(100L, user))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not active");
        }
    }


    @Test
    @DisplayName("❌ requestBlockByOwner throws when expired")
    void requestBlockByOwner_Expired() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));

        try (var mocked = mockStatic(CardExpiryUtil.class)) {
            mocked.when(() -> CardExpiryUtil.isExpired(card)).thenReturn(true);

            assertThatThrownBy(() -> cardService.requestBlockByOwner(100L, user))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not active");
        }
    }


    // ---------- findByIdAndOwner ----------

    @Test
    @DisplayName("✅ findByIdAndOwner success")
    void findByIdAndOwner_Success() {
        when(cardRepository.findByIdAndOwner(100L, user)).thenReturn(Optional.of(card));
        assertThat(cardService.findByIdAndOwner(100L, user)).isEqualTo(card);
    }

    @Test
    @DisplayName("❌ findByIdAndOwner throws when not found")
    void findByIdAndOwner_NotFound() {
        when(cardRepository.findByIdAndOwner(100L, user)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.findByIdAndOwner(100L, user))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------- toResponse() exception ----------

    @Test
    @DisplayName("❌ toResponse throws CorruptedDataException on decrypt error")
    void toResponse_DecryptError() {
        Card corrupt = new Card();
        corrupt.setId(99L);
        corrupt.setCardNumber("bad-data");
        when(encryptionUtil.decrypt("bad-data")).thenThrow(new RuntimeException("fail"));

        assertThatThrownBy(() -> {
            // вызвать приватный метод через публичный путь
            when(cardRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(corrupt)));
            cardService.getAllCards(Pageable.unpaged()).getContent().get(0);
        }).isInstanceOf(CorruptedDataException.class);
    }

    @Test
    @DisplayName("✅ toResponse handles null owner gracefully")
    void toResponse_NullOwner() {
        Card noOwnerCard = new Card();
        noOwnerCard.setId(777L);
        noOwnerCard.setOwner(null);
        noOwnerCard.setCardNumber("encrypted");
        noOwnerCard.setExpiryDate(LocalDate.now().plusYears(1));
        noOwnerCard.setStatus(CardStatus.ACTIVE);
        noOwnerCard.setBalance(BigDecimal.TEN);

        when(cardRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(noOwnerCard)));
        when(encryptionUtil.decrypt("encrypted")).thenReturn("4111111111111111");
        when(cardMaskUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 1111");

        Page<CardResponse> result = cardService.getAllCards(Pageable.unpaged());

        CardResponse resp = result.getContent().get(0);
        assertThat(resp.getOwnerUsername()).isNull();
        assertThat(resp.getMaskedCardNumber()).contains("****");
    }

}
