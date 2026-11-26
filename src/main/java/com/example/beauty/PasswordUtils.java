
package com.example.beauty;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int ITERATIONS = 600000;
    private static final int KEY_LENGTH = 256;

    // Хешування пароля
    public static String hashPassword(String password) {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        byte[] hash = generateHash(password, salt);

        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    // Перевірка пароля
    public static boolean verifyPassword(
            String password, String storedPassword) {

        String[] parts = storedPassword.split(":");

        if (parts.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        byte[] actualHash = generateHash(password, salt);

        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    // Генерація хешу PBKDF2
    private static byte[] generateHash(
            String password, byte[] salt) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Помилка хешування пароля", e
            );

        } finally {
            spec.clearPassword();
        }
    }
}
