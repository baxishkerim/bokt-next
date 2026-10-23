package az.bokt.tenant.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Регистрация новой NBCO супер-админом. За одну транзакцию создаётся:
 * организация, валюта AZN, карта организации (источник средств), BIN-ы, головной филиал,
 * роли (director/moderator/operator) и первый пользователь — ДИРЕКТОР, которому уходят
 * учётные данные по SMS. Карта клиента (куда садятся деньги) задаётся в самой заявке на кредит.
 * Валюта системы — только AZN.
 */
public record RegisterOrganisationRequest(
        @NotBlank String name,
        @NotBlank String login,
        String frontId,

        // Карта организации (источник средств, CHARGE)
        @NotBlank String motherCardPan,
        String motherCardExpiry,
        String merchant,

        // BIN-ы клиентских карт (опционально)
        List<String> bins,

        String headBranchName,

        // Первый пользователь организации — директор
        @NotBlank String directorUsername,
        String directorFirstName,
        String directorLastName,
        @NotBlank String directorPhone
) {}
