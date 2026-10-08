package az.bokt.audit.domain;

import az.bokt.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Событие аудита. Заменяет практику разбора инцидентов по логам и ручной правки таблиц:
 * ключевые действия (вход, выдача/отмена кредита, возврат, изменение пользователей)
 * фиксируются структурированно, с correlationId для сквозной трассировки запроса.
 * tenantId может быть null для действий супер-админа на уровне платформы.
 */
@Getter
@Setter
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "ix_audit_tenant", columnList = "tenant_id"),
        @Index(name = "ix_audit_action", columnList = "action"),
        @Index(name = "ix_audit_entity", columnList = "entity_type,entity_id"),
        @Index(name = "ix_audit_correlation", columnList = "correlation_id")
})
public class AuditEvent extends BaseEntity {

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "entity_type", length = 64)
    private String entityType;

    @Column(name = "entity_id", length = 64)
    private String entityId;

    @Column(length = 2000)
    private String details;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;
}
