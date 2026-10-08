package az.bokt.tenant.dto;

import az.bokt.common.domain.EntityStatus;
import az.bokt.tenant.domain.Branch;

public record BranchResponse(Long id, String name, String frontId, EntityStatus status) {
    public static BranchResponse from(Branch b) {
        return new BranchResponse(b.getId(), b.getName(), b.getFrontId(), b.getStatus());
    }
}
