package az.bokt.audit.service;

import az.bokt.audit.domain.AuditEvent;
import az.bokt.audit.repo.AuditEventRepository;
import az.bokt.common.tenant.TenantContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Запись событий аудита. Пишет в отдельной транзакции (REQUIRES_NEW), чтобы факт аудита
 * сохранялся даже при откате основной бизнес-транзакции.
 */
@Service
public class AuditService {

    private final AuditEventRepository repo;

    public AuditService(AuditEventRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String action, String entityType, String entityId, String details) {
        AuditEvent event = new AuditEvent();
        event.setTenantId(TenantContext.getTenantId());
        event.setUserId(userId);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setDetails(details);
        event.setCorrelationId(MDC.get("correlationId"));
        repo.save(event);
    }
}
