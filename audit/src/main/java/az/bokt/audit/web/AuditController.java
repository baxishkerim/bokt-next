package az.bokt.audit.web;

import az.bokt.audit.domain.AuditEvent;
import az.bokt.audit.repo.AuditEventRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Просмотр журнала аудита. Основной сценарий — трассировка инцидента по correlationId:
 * вместо ручного grep по логам берём все события одного запроса одним вызовом.
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAuthority('REPORT_VIEW')")
public class AuditController {

    private final AuditEventRepository repo;

    public AuditController(AuditEventRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/by-correlation/{correlationId}")
    public List<AuditEvent> byCorrelation(@PathVariable String correlationId) {
        return repo.findByCorrelationIdOrderByCreatedAtAsc(correlationId);
    }

    @GetMapping("/entity/{type}/{id}")
    public List<AuditEvent> byEntity(@PathVariable String type, @PathVariable String id, Pageable pageable) {
        return repo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(type, id, pageable).getContent();
    }
}
