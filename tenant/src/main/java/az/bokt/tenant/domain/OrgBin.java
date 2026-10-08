package az.bokt.tenant.domain;

import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/**
 * BIN карт клиентов NBCO (объединяет BOKT_ORGBINLIST и BOKT_ORGBINS в одну таблицу).
 * Используется для валидации, что карта клиента принадлежит данной NBCO.
 */
@Getter
@Setter
@Entity
@Table(name = "org_bins", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "bin"}))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class OrgBin extends TenantAwareEntity {

    @Column(nullable = false, length = 12)
    private String bin;
}
