package com.example.bankcards.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for masking sensitive card numbers (PANs) before returning them to the client
 * or writing to logs.
 * <p>
 * This utility ensures that only the last 4 digits of a card number remain visible,
 * while the rest are replaced with asterisks. This complies with security standards
 * such as <strong>PCI DSS</strong> (Payment Card Industry Data Security Standard).
 * </p>
 *
 * <h3>Usage guidelines:</h3>
 * <ul>
 *     <li>Should always be applied before exposing any card number in API responses or logs.</li>
 *     <li>Should <strong>never</strong> be used as a substitute for encryption — it only hides data visually.</li>
 *     <li>Can safely be combined with {@link com.example.bankcards.util.EncryptionUtil} for
 *         full data protection (encryption at rest + masking in transit).</li>
 * </ul>
 *
 * <p>
 * Example:
 * <pre>{@code
 * CardMaskUtil maskUtil = new CardMaskUtil();
 * String masked = maskUtil.maskCardNumber("1234567812345678");
 * // Result: "**** **** **** 5678"
 * }</pre>
 * </p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Component
public class CardMaskUtil {

    /**
     * Masks a full card number, leaving only the last four digits visible.
     * <p>
     * If the provided number is {@code null} or shorter than four characters,
     * returns a generic masked value ("****").
     * </p>
     *
     * @param cardNumber the original card number (plain or decrypted)
     * @return a masked string showing only the last four digits
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastDigits = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastDigits;
    }
}
