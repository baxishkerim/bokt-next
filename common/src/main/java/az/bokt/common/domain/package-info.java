/**
 * Определение Hibernate-фильтра тенанта. Объявляется один раз здесь;
 * применяется аннотацией {@code @Filter(name = "tenantFilter")} на каждой
 * tenant-scoped сущности. Включается по запросу в {@code TenantFilterAspect}.
 */
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = Long.class)
)
package az.bokt.common.domain;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
