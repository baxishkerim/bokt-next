package az.bokt.api.tenant;

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
 * Включает Hibernate-фильтр {@code tenantFilter} на время выполнения транзакционных
 * методов сервисов, чтобы изоляция по тенанту работала автоматически, без ручного
 * добавления условия tenant_id в каждый запрос (частый источник ошибок в BOKT).
 * <p>
 * Порядок ({@code @Order(100)}) вместе с {@link TransactionConfig} (tx order 0)
 * гарантирует, что аспект выполняется ВНУТРИ транзакции — фильтр накладывается на
 * ту же Hibernate-сессию, что и запросы метода.
 * <p>
 * Для супер-админа (кросс-тенантный режим) фильтр не включается. При вложенных вызовах
 * сервисов фильтр включает/выключает только внешний вызов (защита по getEnabledFilter).
 */
@Aspect
@Component
@Order(100)
public class TenantFilterAspect {

    private static final String FILTER = "tenantFilter";

    @PersistenceContext
    private EntityManager entityManager;

    @Around("@within(org.springframework.stereotype.Service) && execution(* az.bokt..*Service.*(..))")
    public Object applyTenantFilter(ProceedingJoinPoint pjp) throws Throwable {
        boolean crossTenant = TenantContext.isCrossTenant();
        Long tenantId = TenantContext.getTenantId();

        Session session = entityManager.unwrap(Session.class);
        boolean enabledHere = false;

        if (!crossTenant && tenantId != null && session.getEnabledFilter(FILTER) == null) {
            session.enableFilter(FILTER).setParameter("tenantId", tenantId);
            enabledHere = true;
        }
        try {
            return pjp.proceed();
        } finally {
            if (enabledHere) {
                session.disableFilter(FILTER);
            }
        }
    }
}
