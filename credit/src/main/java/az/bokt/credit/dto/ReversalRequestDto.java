package az.bokt.credit.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Запрос на возврат по кредиту. Если amount не указан — полный возврат суммы кредита,
 * иначе частичный (аналог BOKT_SENDREVERSAL_PARTITIAL).
 */
public record ReversalRequestDto(
        @Positive BigDecimal amount,
        String reason
) {}
