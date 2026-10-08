package az.bokt.tenant.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

/**
 * Регистрация новой NBCO супер-админом. За одну транзакцию создаётся:
 * организация, валюта AZN, материнская карта, карта выдачи, BIN-ы, головной филиал,
 * 5 ролей (director/branch_manager/operator/moderator) и первый пользователь — ДИРЕКТОР,
 * которому уходят учётные данные по SMS. Дальше директор сам управляет своей организацией.
 * Валюта системы — только AZN, поэтому в запросе не указывается.
 */
public record RegisterOrganisationRequest(
        @NotBlank String name,
        @NotBlank String login,
        String frontId,

        // Материнская карта (источник, CHARGE)
        @NotBlank String motherCardPan,
        String motherCardExpiry,
        String merchant,

        // Карта выдачи (TOPUP, клиент снимает наличные)
        @NotBlank String payoutCardPan,
        String payoutCardExpiry,
        BigDecimal payoutInitialBalance,

        // BIN-ы клиентских карт (опционально)
        List<String> bins,

        String headBranchName,

        // Первый пользователь организации — директор
        @NotBlank String directorUsername,
        String directorFirstName,
        String directorLastName,
        @NotBlank String directorPhone
) {}
