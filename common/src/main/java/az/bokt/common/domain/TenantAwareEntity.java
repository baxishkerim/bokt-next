package az.bokt.common.domain;

import az.bokt.common.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

/**
 * Базовый класс для всех сущностей, принадлежащих конкретному тенанту (NBCO).
 * <p>
 * tenant_id проставляется автоматически при вставке из {@link TenantContext},
 * а чтение фильтруется Hibernate-фильтром {@code tenantFilter}
 * (см. {@code @Filter} на конкретных сущностях-наследниках).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class TenantAwareEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @PrePersist
    void assignTenant() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantId();
        }
    }
}
