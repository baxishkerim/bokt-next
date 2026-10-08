package az.bokt.tenant.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Создание филиала вместе с его модератором (начальником филиала) одной формой.
 * Филиал без начальника создать нельзя. Пароль модератору уходит по SMS.
 */
public record CreateBranchWithModeratorRequest(
        @NotBlank String branchName,
        String frontId,
        @NotBlank String moderatorUsername,
        String moderatorFirstName,
        String moderatorLastName,
        @NotBlank String moderatorPhone
) {}
