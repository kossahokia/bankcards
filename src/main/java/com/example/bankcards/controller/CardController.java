package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ApiErrorResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for managing user bank cards and transfers.
 * <p>
 * Provides endpoints for retrieving user cards, checking balances,
 * requesting card blocking, and performing transfers between user cards.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Retrieve paginated lists of user cards (optionally filtered by {@link CardStatus}).</li>
 *   <li>Allow card owners to request card blocking.</li>
 *   <li>Return current card balances.</li>
 *   <li>Perform secure transfers between the user's own cards.</li>
 * </ul>
 *
 * <p>All endpoints require a valid JWT token.</p>
 *
 * @see com.example.bankcards.service.CardService
 * @see com.example.bankcards.service.TransferService
 * @see com.example.bankcards.service.UserService
 * @since 1.0
 */
@Tag(name = "Cards", description = "Endpoints for managing user bank cards and transfers")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserService userService;
    private final TransferService transferService;

    /**
     * Retrieves all cards belonging to the currently authenticated user.
     * Supports pagination and optional filtering by {@link CardStatus}.
     *
     * @param authentication the current authenticated user
     * @param pageable pagination details (page number, size, sorting)
     * @param status optional card status filter
     * @return a paginated list of {@link CardResponse} objects
     */
    @Operation(summary = "Get all cards of the current user", description = "Supports pagination and optional filtering by card status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "422", description = "Corrupted card data: card number could not be decrypted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            Authentication authentication,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) CardStatus status
    ) {
        User user = userService.findByUsername(authentication.getName());
        Page<CardResponse> cards = (status == null)
                ? cardService.getCardsByOwner(user, pageable)
                : cardService.getCardsByOwnerAndStatus(user, status, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Submits a block request for a user’s active card.
     * Only the card owner can perform this operation, and only for active, non-expired cards.
     *
     * @param authentication current authenticated user
     * @param id card ID
     * @return updated {@link CardResponse} with new status
     */
    @Operation(summary = "Request card blocking",
            description = "Allows a user to request blocking of one of their active, non-expired cards.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Block request submitted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Card is not active or has expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found for this user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/request-block")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CardResponse> requestBlock(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(cardService.requestBlockByOwner(id, user));
    }

    /**
     * Returns the balance of a specific card owned by the authenticated user.
     *
     * @param authentication current authenticated user
     * @param id card ID
     * @return current card balance as {@link BigDecimal}
     */
    @Operation(summary = "Get card balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card balance retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "404", description = "Card not found for this user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> getBalance(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.findByUsername(authentication.getName());
        Card card = cardService.findByIdAndOwner(id, user);
        return ResponseEntity.ok(card.getBalance());
    }

    /**
     * Transfers funds between two cards owned by the same user.
     * Both cards must be active, non-expired, and belong to the requester.
     *
     * @param authentication current authenticated user
     * @param req details of the transfer
     * @return HTTP 200 if transfer succeeds
     */
    @Operation(summary = "Transfer funds between user’s own cards")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g., amount ≤ 0 or same card IDs)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Source or destination card not found for this user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Transfer failed (e.g., insufficient funds or inactive card)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest req
    ) {
        User user = userService.findByUsername(authentication.getName());
        transferService.transferBetweenUserCards(
                user,
                req.getFromCardId(),
                req.getToCardId(),
                req.getAmount()
        );
        return ResponseEntity.ok().build();
    }
}
