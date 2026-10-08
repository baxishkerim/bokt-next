package az.bokt.tenant.domain;

import az.bokt.common.domain.EntityStatus;
import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/** Филиал NBCO (BOKT_BRANCHES). */
@Getter
@Setter
@Entity
@Table(name = "branches")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Branch extends TenantAwareEntity {

    @Column(nullable = false, length = 256)
    private String name;

    @Column(name = "front_id", length = 32)
    private String frontId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EntityStatus status = EntityStatus.ACTIVE;
}
