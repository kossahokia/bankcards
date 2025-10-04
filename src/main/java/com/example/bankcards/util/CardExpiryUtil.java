package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

public final class CardExpiryUtil {

    private CardExpiryUtil() {
    }

    public static boolean isExpired(@NotNull Card card) {
        Objects.requireNonNull(card, "Card cannot be null");
        return card.getExpiryDate() != null
                && card.getExpiryDate().isBefore(LocalDate.now());
    }
}
