package com.example.bankcards.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCardRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", message = "Balance must be non-negative")
    private BigDecimal initialBalance;

    @NotBlank(message = "Owner username is required")
    private String ownerUsername;
}
