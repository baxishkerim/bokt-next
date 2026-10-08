package az.bokt.client.domain;

import az.bokt.common.domain.Gender;
import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;

/**
 * Клиент NBCO — заёмщик. Замена BOKT_CLIENTS.
 * Содержит персональные данные, поэтому tenant-scoped и не должен утекать между организациями.
 * PIN (ФИН) уникален в рамках тенанта.
 */
@Getter
@Setter
@Entity
@Table(
        name = "clients",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "pin"}),
        indexes = {
                @Index(name = "ix_client_phone", columnList = "phone"),
                @Index(name = "ix_client_last_name", columnList = "last_name")
        }
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Client extends TenantAwareEntity {

    /** ФИН / персональный идентификационный номер. */
    @Column(nullable = false, length = 32)
    private String pin;

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

    @Column(length = 512)
    private String address;

    /** Секретное слово для идентификации клиента в колл-центре. */
    @Column(name = "secret_word", length = 128)
    private String secretWord;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "modified_by")
    private Long modifiedBy;
}
