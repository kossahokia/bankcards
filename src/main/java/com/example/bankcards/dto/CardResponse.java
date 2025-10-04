// CardResponse.java
package com.example.bankcards.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private String maskedCardNumber;
    private String ownerUsername;
    private LocalDate expiryDate;
    private String status;
    private BigDecimal balance;
}

