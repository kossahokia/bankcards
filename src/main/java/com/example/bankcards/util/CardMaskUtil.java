package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardMaskUtil {
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastDigits = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastDigits;
    }
}