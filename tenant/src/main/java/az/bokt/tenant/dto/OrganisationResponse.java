package az.bokt.tenant.dto;

import az.bokt.tenant.domain.Organisation;

public record OrganisationResponse(
        Long id,
        String name,
        String login,
        String frontId,
        boolean active,
        boolean platform
) {
    public static OrganisationResponse from(Organisation o) {
        return new OrganisationResponse(o.getId(), o.getName(), o.getLogin(),
                o.getFrontId(), o.isActive(), o.isPlatform());
    }
}
