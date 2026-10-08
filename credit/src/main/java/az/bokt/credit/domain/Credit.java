package az.bokt.credit.domain;

import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

/**
 * Кредит — заявка на выдачу мгновенного кредита. Замена BOKT_CREDITS.
 * <p>
 * Жизненный цикл: NEW → (APPROVED | CANCELLED | POSTPONED).
 * При подтверждении выполняется платёжная цепочка CHARGE (списание с карты NBCO) +
 * TOPUP (зачисление на карту клиента); rrn и GUID обеих операций сохраняются здесь,
 * чтобы не искать их вручную по логам, как это было в BOKT.
 * Сумма — в минорных единицах (см. {@link Money}).
 */
@Getter
@Setter
@Entity
@Table(name = "credits", indexes = {
        @Index(name = "ix_credit_client", columnList = "client_id"),
        @Index(name = "ix_credit_status", columnList = "status"),
        @Index(name = "ix_credit_client_pan", columnList = "client_card_pan"),
        @Index(name = "ix_credit_file", columnList = "file_id")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class Credit extends TenantAwareEntity {

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /** Карта клиента, на которую зачисляются средства (TOPUP). */
    @Column(name = "client_card_pan", length = 32)
    private String clientCardPan;

    /** Сумма в минорных единицах. */
    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CreditStatus status = CreditStatus.NEW;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "secret_word", length = 128)
    private String secretWord;

    @Column(length = 512)
    private String description;

    /** Если кредит загружен из файла — ссылка на файл-источник. */
    @Column(name = "file_id")
    private Long fileId;

    // --- платёжные атрибуты ---

    /** GUID операции CHARGE в процессинге: "CH" + creditId. */
    @Column(name = "charge_guid", length = 32)
    private String chargeGuid;

    /** GUID операции TOPUP в процессинге: "TP" + creditId. */
    @Column(name = "topup_guid", length = 32)
    private String topupGuid;

    @Column(name = "charge_rrn", length = 32)
    private String chargeRrn;

    @Column(name = "topup_rrn", length = 32)
    private String topupRrn;

    // --- аудит операций ---

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    public boolean isApprovable() {
        return status == CreditStatus.NEW || status == CreditStatus.POSTPONED;
    }

    public boolean isCancellable() {
        return status == CreditStatus.NEW || status == CreditStatus.POSTPONED;
    }
}
