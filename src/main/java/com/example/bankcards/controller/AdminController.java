package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.dto.enums.UsernameMatchType;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ApiErrorResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "Endpoints for managing users and cards (admin only)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CardService cardService;
    private final UserService userService;

    // ---------- USERS ----------

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed: invalid request body",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid JWT",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: admin role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest req) {
        User user = userService.createUser(
                req.getUsername(),
                req.getPassword(),
                req.getFullName(),
                req.getRoleName(),
                req.isEnabled()
        );
        return ResponseEntity.ok(
                new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.isEnabled())
        );
    }

    @Operation(summary = "Get all users (with filters and pagination)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: admin role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "CONTAINS") UsernameMatchType matchType,
            @RequestParam(required = false) Boolean enabled,
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                userService.getAllUsers(username, enabled, matchType, pageable)
                        .map(u -> new UserDto(u.getId(), u.getUsername(), u.getFullName(), u.isEnabled()))
        );
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(
                new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.isEnabled())
        );
    }

    @Operation(summary = "Delete user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update user enabled status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        User updated = userService.updateUserEnabledStatus(id, enabled);
        return ResponseEntity.ok(
                new UserDto(updated.getId(), updated.getUsername(), updated.getFullName(), updated.isEnabled())
        );
    }

    @Operation(summary = "Assign role to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role successfully assigned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserDto> assignRole(@PathVariable Long id, @RequestParam String roleName) {
        User updated = userService.assignRole(id, roleName);
        return ResponseEntity.ok(
                new UserDto(updated.getId(), updated.getUsername(), updated.getFullName(), updated.isEnabled())
        );
    }

    @Operation(summary = "Remove role from user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role successfully removed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/users/{id}/role/remove")
    public ResponseEntity<UserDto> removeRole(@PathVariable Long id,
                                              @RequestParam String roleName) {
        User updated = userService.removeRole(id, roleName);
        return ResponseEntity.ok(
                new UserDto(updated.getId(), updated.getUsername(), updated.getFullName(), updated.isEnabled())
        );
    }

    @Operation(summary = "Get cards of a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users/{id}/cards")
    public ResponseEntity<Page<CardResponse>> getUserCards(@PathVariable Long id,
                                                           @ParameterObject Pageable pageable) {
        userService.getUserById(id); // проверка на существование
        return ResponseEntity.ok(cardService.getCardsByUserId(id, pageable));
    }

    // ---------- CARDS ----------

    @Operation(summary = "Create a new card for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed: invalid request body",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/cards")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest req) {
        Card card = new Card();
        card.setCardNumber(req.getCardNumber());
        card.setExpiryDate(req.getExpiryDate());
        card.setBalance(req.getInitialBalance());
        return ResponseEntity.ok(cardService.createCardWithOwnerUsername(card, req.getOwnerUsername()));
    }

    @Operation(summary = "Get all cards (with optional status filter)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class)))
    })
    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponse>> getAllCards(@ParameterObject Pageable pageable,
                                                          @RequestParam(required = false) CardStatus status) {
        return ResponseEntity.ok(
                status == null
                        ? cardService.getAllCards(pageable)
                        : cardService.getCardsByStatus(status, pageable)
        );
    }

    @Operation(summary = "Update card status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card status successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/cards/{id}/status")
    public ResponseEntity<CardResponse> updateCardStatus(@PathVariable Long id, @RequestParam CardStatus status) {
        return ResponseEntity.ok(cardService.updateCardStatus(id, status));
    }

    @Operation(summary = "Delete card by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Card not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
