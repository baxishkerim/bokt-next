package az.bokt.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Настройки JWT. Ключи RSA задаются PEM-строками (или путём к файлу) в конфигурации;
 * если не заданы — в dev-режиме генерируется временная пара (см. RsaKeyProvider).
 */
@ConfigurationProperties(prefix = "bokt.jwt")
public record JwtProperties(
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String privateKeyPem,
        String publicKeyPem
) {
    public JwtProperties {
        if (issuer == null) {
            issuer = "bokt-next";
        }
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofMinutes(10);
        }
        if (refreshTokenTtl == null) {
            refreshTokenTtl = Duration.ofDays(7);
        }
    }
}
