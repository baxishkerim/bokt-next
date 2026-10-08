package az.bokt.api.tenant;

import az.bokt.common.tenant.BranchContext;
import az.bokt.common.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Включает Hibernate-фильтры изоляции на время транзакционных методов сервисов:
 * <ul>
 *   <li>{@code tenantFilter} — по организации (кроме супер-админа);</li>
 *   <li>{@code branchFilter} — по филиалу, только если у пользователя задан филиал
 *       (модератор/оператор). Директор без филиала видит всю организацию.</li>
 * </ul>
 * Порядок @Order(100) + TransactionConfig(order 0) гарантируют выполнение ВНУТРИ транзакции.
 * Защита от вложенных вызовов: включает/выключает только тот вызов, который включил фильтр.
 */
@Aspect
@Component
@Order(100)
public class TenantFilterAspect {

    private static final String TENANT_FILTER = "tenantFilter";
    private static final String BRANCH_FILTER = "branchFilter";

    @PersistenceContext
    private EntityManager entityManager;

    @Around("@within(org.springframework.stereotype.Service) && execution(* az.bokt..*Service.*(..))")
    public Object applyFilters(ProceedingJoinPoint pjp) throws Throwable {
        boolean crossTenant = TenantContext.isCrossTenant();
        Long tenantId = TenantContext.getTenantId();
        Long branchId = BranchContext.getBranchId();

        Session session = entityManager.unwrap(Session.class);
        boolean tenantEnabledHere = false;
        boolean branchEnabledHere = false;

        if (!crossTenant && tenantId != null && session.getEnabledFilter(TENANT_FILTER) == null) {
            session.enableFilter(TENANT_FILTER).setParameter("tenantId", tenantId);
            tenantEnabledHere = true;
        }
        if (!crossTenant && branchId != null && session.getEnabledFilter(BRANCH_FILTER) == null) {
            session.enableFilter(BRANCH_FILTER).setParameter("branchId", branchId);
            branchEnabledHere = true;
        }
        try {
            return pjp.proceed();
        } finally {
            if (branchEnabledHere) {
                session.disableFilter(BRANCH_FILTER);
            }
            if (tenantEnabledHere) {
                session.disableFilter(TENANT_FILTER);
            }
        }
    }
}
