package az.bokt.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Хэширование и генерация высокоэнтропийных секретов (refresh-токены, OTP-reference).
 * Для паролей используется PasswordEncoder (bcrypt) — здесь только быстрый SHA-256,
 * т.к. значения случайны и не подвержены brute-force.
 */
public final class HashUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private HashUtil() {}

    /** Криптостойкий url-safe токен заданной длины в байтах. */
    public static String randomToken(int bytes) {
        byte[] buf = new byte[bytes];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** Числовой OTP заданной длины (по умолчанию 6 цифр). */
    public static String numericOtp(int digits) {
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Сравнение без утечки времени. */
    public static boolean matches(String rawValue, String expectedHashHex) {
        return MessageDigest.isEqual(
                sha256Hex(rawValue).getBytes(StandardCharsets.UTF_8),
                expectedHashHex.getBytes(StandardCharsets.UTF_8));
    }
}
