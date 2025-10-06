package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.dto.enums.UsernameMatchType;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminController.class,
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
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- USERS ----------

    @Test
    @DisplayName("✅ createUser returns 201 and user JSON")
    void createUser_Success() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("alice");
        req.setPassword("pwd123");
        req.setFullName("Alice");
        req.setRoleName("USER");
        req.setEnabled(true);

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setFullName("Alice");
        user.setEnabled(true);

        when(userService.createUser(any(), any(), any(), any(), anyBoolean())).thenReturn(user);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()) // ✅ теперь ожидаем 201
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.fullName").value("Alice"))
                .andExpect(jsonPath("$.enabled").value(true));
    }


    @Test
    @DisplayName("❌ createUser returns 400 if username is missing")
    void createUser_Invalid_Returns400() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("");
        req.setPassword("pwd");
        req.setFullName("Alice");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ getAllUsers returns page of users")
    void getAllUsers_Success() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setUsername("alice");
        u.setFullName("Alice");
        u.setEnabled(true);

        when(userService.getAllUsers(anyString(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(u), PageRequest.of(0, 1), 1));

        mockMvc.perform(get("/api/admin/users")
                        .param("username", "a")
                        .param("matchType", UsernameMatchType.CONTAINS.name())
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("alice"));
    }

    @Test
    @DisplayName("✅ getUserById returns 200")
    void getUserById_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setFullName("Alice");
        user.setEnabled(true);

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("✅ deleteUser returns 204")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("✅ updateUserStatus returns 200")
    void updateUserStatus_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setFullName("Alice");
        user.setEnabled(false);

        when(userService.updateUserEnabledStatus(1L, false)).thenReturn(user);

        mockMvc.perform(patch("/api/admin/users/1/status")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("✅ assignRole returns 200")
    void assignRole_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setFullName("Alice");
        user.setEnabled(true);

        when(userService.assignRole(1L, "USER")).thenReturn(user);

        mockMvc.perform(patch("/api/admin/users/1/role")
                        .param("roleName", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("✅ removeRole returns 200")
    void removeRole_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setFullName("Alice");
        user.setEnabled(true);

        when(userService.removeRole(1L, "USER")).thenReturn(user);

        mockMvc.perform(patch("/api/admin/users/1/role/remove")
                        .param("roleName", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("✅ getUserCards returns 200")
    void getUserCards_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new User());
        CardResponse cardResponse = new CardResponse(
                1L,
                "****1111",
                "alice",
                LocalDate.now().plusYears(1),
                CardStatus.ACTIVE.name(),
                BigDecimal.TEN
        );

        when(cardService.getCardsByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cardResponse)));

        mockMvc.perform(get("/api/admin/users/1/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("****1111"));
    }

    // ---------- CARDS ----------

    @Test
    @DisplayName("✅ createCard returns 200 and JSON")
    void createCard_Success() throws Exception {
        CreateCardRequest req = new CreateCardRequest();
        req.setOwnerUsername("alice");
        req.setCardNumber("1234567812345678");
        req.setExpiryDate(LocalDate.now().plusYears(1));
        req.setInitialBalance(BigDecimal.valueOf(100));

        CardResponse resp = new CardResponse(
                1L,
                "****5678",
                "alice",
                LocalDate.now().plusYears(1),
                "ACTIVE",
                BigDecimal.valueOf(100)
        );

        when(cardService.createCardWithOwnerUsername(any(), anyString())).thenReturn(resp);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedCardNumber").value("****5678"))
                .andExpect(jsonPath("$.ownerUsername").value("alice"));
    }

    @Test
    @DisplayName("❌ createCard returns 400 when missing fields")
    void createCard_Invalid_Returns400() throws Exception {
        CreateCardRequest req = new CreateCardRequest();
        req.setOwnerUsername(null);
        req.setCardNumber("");
        req.setExpiryDate(null);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ getAllCards returns 200 with status filter")
    void getAllCards_WithStatus() throws Exception {
        CardResponse resp = new CardResponse(
                1L,
                "****5678",
                "alice",
                LocalDate.now().plusYears(1),
                "ACTIVE",
                BigDecimal.TEN
        );

        when(cardService.getCardsByStatus(eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resp)));

        mockMvc.perform(get("/api/admin/cards").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("✅ updateCardStatus returns 200")
    void updateCardStatus_Success() throws Exception {
        CardResponse resp = new CardResponse(
                1L,
                "****5678",
                "alice",
                LocalDate.now().plusYears(1),
                "BLOCKED",
                BigDecimal.TEN
        );

        when(cardService.updateCardStatus(1L, CardStatus.BLOCKED)).thenReturn(resp);

        mockMvc.perform(patch("/api/admin/cards/1/status").param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @DisplayName("✅ deleteCard returns 204")
    void deleteCard_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/cards/1"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(1L);
    }

    @Test
    @DisplayName("✅ getAllCards returns 200 without status filter")
    void getAllCards_WithoutStatus() throws Exception {
        CardResponse resp = new CardResponse(
                1L,
                "****5678",
                "alice",
                LocalDate.now().plusYears(1),
                "ACTIVE",
                BigDecimal.TEN
        );

        when(cardService.getAllCards(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resp)));

        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("****5678"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

}
