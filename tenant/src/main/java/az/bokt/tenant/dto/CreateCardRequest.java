package az.bokt.tenant.dto;

import az.bokt.tenant.domain.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Добавление карты организации. balance — начальный остаток/лимит (в основных единицах):
 * для PAYOUT это рабочий лимит выдачи, для MOTHER — информационный остаток (пока задел).
 */
public record CreateCardRequest(
        @NotNull CardType type,
        @NotBlank String pan,
        String expiry,
        String merchant,
        BigDecimal balance
) {}
