package az.bokt.auth.domain;

import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.HashSet;
import java.util.Set;

/**
 * Роль внутри тенанта. Каждая NBCO создаёт собственные роли и назначает им права
 * (в BOKT роли были фиксированы: 1 operator, 2 moderator — здесь это гибко).
 */
@Getter
@Setter
@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Role extends TenantAwareEntity {

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 256)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();
}
