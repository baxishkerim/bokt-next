package az.bokt.tenant.domain;

import az.bokt.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Справочник валют (BOKT_CURRENCIES). Глобальный, общий для всех тенантов. */
@Getter
@Setter
@Entity
@Table(name = "currencies")
public class Currency extends BaseEntity {

    /** Числовой код валюты (ISO 4217 numeric), напр. 944 для AZN. */
    @Column(nullable = false, unique = true, length = 8)
    private String code;

    /** Буквенное обозначение (AZN, USD). */
    @Column(nullable = false, length = 8)
    private String value;
}
