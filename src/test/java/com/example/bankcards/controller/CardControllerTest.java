package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CardController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.example\\.bankcards\\.security\\..*"
        )
)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @MockBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private Authentication authentication;

    // ---------- getMyCards ----------

    @Test
    @DisplayName("✅ getMyCards returns 200 without status filter")
    void getMyCards_WithoutStatus() throws Exception {
        User user = new User();
        user.setUsername("alice");

        when(authentication.getName()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(user);

        CardResponse card = new CardResponse(
                1L, "****1111", "alice",
                LocalDate.now().plusYears(1),
                "ACTIVE", BigDecimal.TEN
        );

        when(cardService.getCardsByOwner(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card), PageRequest.of(0, 1), 1));

        mockMvc.perform(get("/api/cards")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("****1111"));
    }

    @Test
    @DisplayName("✅ getMyCards returns 200 with status filter")
    void getMyCards_WithStatus() throws Exception {
        User user = new User();
        user.setUsername("alice");

        when(authentication.getName()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(user);

        CardResponse card = new CardResponse(
                2L, "****2222", "alice",
                LocalDate.now().plusYears(2),
                "BLOCKED", BigDecimal.valueOf(50)
        );

        when(cardService.getCardsByOwnerAndStatus(eq(user), eq(CardStatus.BLOCKED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        mockMvc.perform(get("/api/cards")
                        .principal(authentication)
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("BLOCKED"));
    }

    // ---------- requestBlock ----------

    @Test
    @DisplayName("✅ requestBlock returns 200")
    void requestBlock_Success() throws Exception {
        User user = new User();
        user.setUsername("alice");

        when(authentication.getName()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(user);

        CardResponse cardResponse = new CardResponse(
                1L, "****1111", "alice",
                LocalDate.now().plusYears(1),
                "BLOCK_PENDING", BigDecimal.TEN
        );

        when(cardService.requestBlockByOwner(1L, user)).thenReturn(cardResponse);

        mockMvc.perform(post("/api/cards/1/request-block")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCK_PENDING"));
    }

    // ---------- getBalance ----------

    @Test
    @DisplayName("✅ getBalance returns 200 with correct balance")
    void getBalance_Success() throws Exception {
        User user = new User();
        user.setUsername("alice");

        Card card = new Card();
        card.setBalance(BigDecimal.valueOf(123.45));

        when(authentication.getName()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(user);
        when(cardService.findByIdAndOwner(1L, user)).thenReturn(card);

        mockMvc.perform(get("/api/cards/1/balance")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("123.45"));
    }

    // ---------- transfer ----------

    @Test
    @DisplayName("✅ transfer returns 200 on success")
    void transfer_Success() throws Exception {
        User user = new User();
        user.setUsername("alice");

        when(authentication.getName()).thenReturn("alice");
        when(userService.findByUsername("alice")).thenReturn(user);

        TransferRequest req = new TransferRequest();
        req.setFromCardId(1L);
        req.setToCardId(2L);
        req.setAmount(BigDecimal.valueOf(50));

        mockMvc.perform(post("/api/cards/transfer")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(transferService).transferBetweenUserCards(eq(user), eq(1L), eq(2L), eq(BigDecimal.valueOf(50)));
    }

    @Test
    @DisplayName("❌ transfer returns 400 when amount is missing")
    void transfer_Invalid_Returns400() throws Exception {
        TransferRequest req = new TransferRequest(); // amount = null

        when(authentication.getName()).thenReturn("alice");

        mockMvc.perform(post("/api/cards/transfer")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
