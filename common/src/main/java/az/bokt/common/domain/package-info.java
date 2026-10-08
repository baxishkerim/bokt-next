/**
 * Определения Hibernate-фильтров изоляции данных, объявляются один раз здесь:
 * <ul>
 *   <li>{@code tenantFilter} — изоляция по организации (NBCO), применяется ко всем
 *       tenant-scoped сущностям;</li>
 *   <li>{@code branchFilter} — изоляция по филиалу, применяется к кредитам и клиентам;
 *       включается только когда у текущего пользователя задан филиал (модератор/оператор).
 *       Директор без филиала видит всю организацию.</li>
 * </ul>
 * Оба включаются в {@code TenantFilterAspect}.
 */
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = Long.class)
)
@FilterDef(
        name = "branchFilter",
        parameters = @ParamDef(name = "branchId", type = Long.class)
)
package az.bokt.common.domain;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
