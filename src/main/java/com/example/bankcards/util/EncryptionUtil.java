package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utility class providing symmetric encryption and decryption using the AES algorithm.
 * <p>
 * This component is primarily used for encrypting and decrypting sensitive card data
 * (e.g., card numbers) before storing them in the database.
 * </p>
 *
 * <h3>Implementation details:</h3>
 * <ul>
 *     <li>Uses AES (Advanced Encryption Standard) with a 128-bit secret key.</li>
 *     <li>The key is derived from the {@code app.encryption.secret} application property.</li>
 *     <li>Encrypted data is Base64-encoded for safe text-based storage.</li>
 * </ul>
 *
 * <h3>Security considerations:</h3>
 * <ul>
 *     <li>The secret key must be at least 16 bytes long to ensure strong encryption.</li>
 *     <li>The same key is used for both encryption and decryption (symmetric algorithm).</li>
 *     <li>In production environments, the key should be securely stored (e.g., in Vault or AWS Secrets Manager)
 *         and never hard-coded or version-controlled.</li>
 *     <li>This implementation is suitable for data-at-rest protection, not for password storage or transmission security.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * String encrypted = encryptionUtil.encrypt("1234567890123456");
 * String decrypted = encryptionUtil.decrypt(encrypted);
 * }</pre>
 * </p>
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKey;

    /**
     * Constructs the encryption utility using the configured secret key.
     *
     * @param secret the encryption key loaded from application properties
     */
    public EncryptionUtil(@Value("${app.encryption.secret}") String secret) {
        byte[] keyBytes = Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 16); // AES-128 key
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Encrypts a plaintext value using AES.
     *
     * @param value plaintext string to encrypt
     * @return encrypted value encoded with Base64
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }

    /**
     * Decrypts a previously AES-encrypted and Base64-encoded value.
     *
     * @param encrypted the encrypted Base64 string
     * @return decrypted plaintext string
     * @throws RuntimeException if decryption fails (e.g., corrupted input or invalid key)
     */
    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }
}
