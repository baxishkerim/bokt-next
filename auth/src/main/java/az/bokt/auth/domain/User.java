package az.bokt.auth.domain;

import az.bokt.common.domain.EntityStatus;
import az.bokt.common.domain.Gender;
import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Пользователь системы — сотрудник NBCO. Замена BOKT_USERS.
 * <p>
 * Отличия от старой модели:
 * <ul>
 *   <li>пароль хранится как Argon2/BCrypt-хэш (не sha256 из онлайн-сервиса);</li>
 *   <li>роли — гибкие ({@link Role}), а не жёсткие 1/2;</li>
 *   <li>OTP не хранится статичным числом — он генерируется на каждый вход (см. OtpChallenge).</li>
 * </ul>
 * Супер-админ платформы: {@code superAdmin=true}; относится к служебному
 * "платформенному" тенанту, но при входе получает кросс-тенантный доступ ко всем данным.
 */
@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "username"}))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class User extends TenantAwareEntity {

    @Column(nullable = false, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", length = 128)
    private String firstName;

    @Column(name = "last_name", length = 128)
    private String lastName;

    @Column(name = "middle_name", length = 128)
    private String middleName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gender gender = Gender.UNKNOWN;

    @Column(length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "password_expiry")
    private LocalDate passwordExpiry;

    @Column(name = "super_admin", nullable = false)
    private boolean superAdmin = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public boolean isActive() {
        return status == EntityStatus.ACTIVE;
    }

    public boolean isPasswordExpired() {
        return passwordExpiry != null && passwordExpiry.isBefore(LocalDate.now());
    }

    public Set<Permission> effectivePermissions() {
        Set<Permission> perms = new HashSet<>();
        if (superAdmin) {
            perms.addAll(Set.of(Permission.values()));
            return perms;
        }
        for (Role r : roles) {
            perms.addAll(r.getPermissions());
        }
        return perms;
    }
}
