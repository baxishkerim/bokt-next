package az.bokt.tenant.domain;

import az.bokt.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * NBCO — небанковская кредитная организация. Это и есть "тенант" платформы.
 * Замена BOKT_ORGANISATIONS. Сама по себе не tenant-scoped (это корень тенанта);
 * все прочие данные ссылаются на неё через tenant_id.
 */
@Getter
@Setter
@Entity
@Table(name = "organisations")
public class Organisation extends BaseEntity {

    @Column(nullable = false, length = 256)
    private String name;

    /** Короткий логин организации (BOKT_ORGANISATIONS.login) — используется при входе. */
    @Column(nullable = false, unique = true, length = 32)
    private String login;

    /** Короткий числовой/строковый идентификатор фронта (BOKT_ORGANISATIONS.frontid). */
    @Column(name = "front_id", length = 32)
    private String frontId;

    @Column(nullable = false)
    private boolean active = true;

    /** Признак служебного платформенного тенанта (для супер-админа). */
    @Column(name = "platform", nullable = false)
    private boolean platform = false;
}
