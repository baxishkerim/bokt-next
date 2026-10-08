package az.bokt.auth.domain;

import az.bokt.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Одноразовый код подтверждения входа. Генерируется на каждую попытку входа
 * (в отличие от статичного 6-значного OTP в BOKT_USERS) и отправляется по SMS.
 * Хранится хэш кода, есть TTL и ограничение числа попыток.
 */
@Getter
@Setter
@Entity
@Table(name = "otp_challenges", indexes = @Index(name = "ix_otp_ref", columnList = "reference"))
public class OtpChallenge extends BaseEntity {

    /** Одноразовый идентификатор challenge, возвращается клиенту после шага 1 (логин/пароль). */
    @Column(nullable = false, length = 64, unique = true)
    private String reference;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "code_hash", nullable = false, length = 128)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 5;

    @Column(nullable = false)
    private boolean consumed = false;

    public boolean isUsable() {
        return !consumed && attempts < maxAttempts && expiresAt.isAfter(Instant.now());
    }
}
