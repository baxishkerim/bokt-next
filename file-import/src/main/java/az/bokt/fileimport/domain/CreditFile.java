package az.bokt.fileimport.domain;

import az.bokt.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/**
 * Файл-источник загрузки кредитов (BOKT_FILES). Хранит метаданные загрузки и её итог;
 * созданные кредиты ссылаются на файл через Credit.fileId (просмотр — /credit/loaded).
 */
@Getter
@Setter
@Entity
@Table(name = "credit_files")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class CreditFile extends TenantAwareEntity {

    @Column(name = "file_name", nullable = false, length = 256)
    private String fileName;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "imported_rows", nullable = false)
    private int importedRows;

    @Column(name = "failed_rows", nullable = false)
    private int failedRows;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ImportStatus status = ImportStatus.PROCESSING;

    @Column(length = 2000)
    private String errors;

    public enum ImportStatus {
        PROCESSING,
        COMPLETED,
        COMPLETED_WITH_ERRORS,
        FAILED
    }
}
