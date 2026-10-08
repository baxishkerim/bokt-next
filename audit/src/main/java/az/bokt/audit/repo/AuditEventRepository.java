package az.bokt.audit.repo;

import az.bokt.audit.domain.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    Page<AuditEvent> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    List<AuditEvent> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);

    Page<AuditEvent> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId, Pageable pageable);
}
