package az.bokt.tenant.dto;

import az.bokt.common.domain.EntityStatus;
import az.bokt.tenant.service.OrganisationService.BranchWithModerator;

/** Ответ на создание филиала вместе с его модератором. */
public record BranchWithModeratorResponse(
        Long branchId,
        String branchName,
        String frontId,
        EntityStatus status,
        Long moderatorId,
        String moderatorUsername
) {
    public static BranchWithModeratorResponse from(BranchWithModerator r) {
        return new BranchWithModeratorResponse(
                r.branch().getId(), r.branch().getName(), r.branch().getFrontId(),
                r.branch().getStatus(), r.moderatorId(), r.moderatorUsername());
    }
}
