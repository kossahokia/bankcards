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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrative controller providing endpoints for managing users and cards.
 * <p>
 * Accessible only to users with the {@code ADMIN} role.
 * Provides CRUD operations on users and cards, as well as role management
 * and account enable/disable toggling.
 * </p>
 *
 * @see com.example.bankcards.service.UserService
 * @see com.example.bankcards.service.CardService
 * @since 1.0
 */
@Tag(name = "Admin", description = "Endpoints for managing users and cards (admin only)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CardService cardService;
    private final UserService userService;

    // ---------- USER MANAGEMENT ----------

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or username already taken",
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.isEnabled()));
    }

    @Operation(summary = "Retrieve all users", description = "Supports username filtering and enabled status flag.")
    @ApiResponse(responseCode = "200", description = "Users successfully retrieved")
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
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
    @ApiResponses({
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

    @Operation(summary = "Toggle user enabled status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status updated"),
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role assigned"),
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role removed"),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/users/{id}/role/remove")
    public ResponseEntity<UserDto> removeRole(@PathVariable Long id, @RequestParam String roleName) {
        User updated = userService.removeRole(id, roleName);
        return ResponseEntity.ok(
                new UserDto(updated.getId(), updated.getUsername(), updated.getFullName(), updated.isEnabled())
        );
    }

    @Operation(summary = "Get cards of a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users/{id}/cards")
    public ResponseEntity<Page<CardResponse>> getUserCards(@PathVariable Long id, @ParameterObject Pageable pageable) {
        userService.getUserById(id);
        return ResponseEntity.ok(cardService.getCardsByUserId(id, pageable));
    }

    // ---------- CARD MANAGEMENT ----------

    @Operation(summary = "Create a new card for user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card created"),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicate card"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/cards")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest req) {
        Card card = new Card();
        card.setCardNumber(req.getCardNumber());
        card.setExpiryDate(req.getExpiryDate());
        card.setBalance(req.getInitialBalance());
        return ResponseEntity.ok(cardService.createCardWithOwnerUsername(card, req.getOwnerUsername()));
    }

    @Operation(summary = "Retrieve all cards (with optional status filter)")
    @ApiResponse(responseCode = "200", description = "Cards retrieved")
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PatchMapping("/cards/{id}/status")
    public ResponseEntity<CardResponse> updateCardStatus(@PathVariable Long id, @RequestParam CardStatus status) {
        return ResponseEntity.ok(cardService.updateCardStatus(id, status));
    }

    @Operation(summary = "Delete card by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card deleted"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
