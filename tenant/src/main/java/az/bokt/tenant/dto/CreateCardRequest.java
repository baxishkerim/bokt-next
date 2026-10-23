package az.bokt.tenant.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Добавление карты организации (источник средств). balance — информационный остаток
 * в основных единицах (факт берётся из процессинга).
 */
public record CreateCardRequest(
        @NotBlank String pan,
        String expiry,
        String merchant,
        BigDecimal balance
) {}
