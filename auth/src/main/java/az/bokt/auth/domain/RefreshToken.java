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
 * Непрозрачный (opaque) refresh-токен. В БД хранится только его хэш.
 * Заменяет BOKT_SESSIONS: там сессии "жили" пока их не отключат вручную флагом status —
 * здесь у токена есть срок жизни и возможность отзыва (revoked / rotation).
 */
@Getter
@Setter
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "ix_refresh_token_hash", columnList = "token_hash"),
        @Index(name = "ix_refresh_user", columnList = "user_id")
})
public class RefreshToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "user_agent", length = 256)
    private String userAgent;

    public boolean isActive() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }
}
