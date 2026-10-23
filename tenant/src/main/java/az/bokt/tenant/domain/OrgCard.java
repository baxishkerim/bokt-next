package az.bokt.tenant.domain;

import az.bokt.common.domain.EntityStatus;
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

/**
 * Карта организации — источник средств при выдаче кредита (CHARGE).
 * Пул таких карт; при выдаче выбирается первая активная. Карта клиента (куда садятся деньги)
 * динамическая и задаётся в самой заявке, в пуле организации не хранится.
 * balanceMinor — информационный остаток (факт берётся из процессинга через getCard).
 * Заблокированная карта (status=DISABLED) в выдаче не участвует. Валюта только AZN.
 */
@Getter
@Setter
@Entity
@Table(name = "org_cards", indexes = {
        @Index(name = "ix_org_cards_tenant", columnList = "tenant_id")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class OrgCard extends TenantAwareEntity {

    @Column(nullable = false, length = 32)
    private String pan;

    @Column(length = 8)
    private String expiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "balance_minor", nullable = false)
    private long balanceMinor = 0;

    @Column(length = 32)
    private String merchant;

    public boolean isActive() {
        return status == EntityStatus.ACTIVE;
    }
}
