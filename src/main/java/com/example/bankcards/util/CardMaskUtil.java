package com.example.bankcards.util;

public class CardMaskUtil {

    private CardMaskUtil() {
        // утильный класс — приватный конструктор
    }

    /**
     * Маскирует номер карты, показывая только последние 4 цифры.
     * Пример: "1234567812345678" -> "**** **** **** 5678"
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastDigits = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastDigits;
    }
}
