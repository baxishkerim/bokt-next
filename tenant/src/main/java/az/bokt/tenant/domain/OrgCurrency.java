package az.bokt.tenant.domain;

import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/** С какими валютами работает NBCO (BOKT_ORGCURRENCIES). */
@Getter
@Setter
@Entity
@Table(name = "org_currencies", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "currency_id"}))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class OrgCurrency extends TenantAwareEntity {

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;
}
