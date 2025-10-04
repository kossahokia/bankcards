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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "Cards", description = "Endpoints for managing user bank cards and transfers")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserService userService;
    private final TransferService transferService;

    @Operation(summary = "Get all cards of the current user (with pagination and optional status filter)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid JWT",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Corrupted card data: card information could not be decrypted",
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
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        Page<CardResponse> page = (status == null)
                ? cardService.getCardsByOwner(user, pageable)
                : cardService.getCardsByOwnerAndStatus(user, status, pageable);

        return ResponseEntity.ok(page);
    }


    @Operation(summary = "Request blocking of a card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card block request successfully submitted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Card is not active, cannot request block",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid JWT",
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
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        CardResponse updated = cardService.requestBlockByOwner(id, user);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Get balance of a specific card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card balance successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid JWT",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
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
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        Card card = cardService.findByIdAndOwner(id, user);
        return ResponseEntity.ok(card.getBalance());
    }

    @Operation(summary = "Transfer money between user’s own cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successfully completed"),
            @ApiResponse(responseCode = "400", description = "Validation failed: invalid request body (e.g. missing fields, amount <= 0)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid JWT",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found for this user (either source or destination card is invalid)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Transfer failed due to business rule violation (e.g. insufficient funds)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest req
    ) {
        String username = authentication.getName();
        var user = userService.findByUsername(username);

        transferService.transferBetweenUserCards(
                user,
                req.getFromCardId(),
                req.getToCardId(),
                req.getAmount()
        );

        return ResponseEntity.ok().build();
    }
}
