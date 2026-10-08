package az.bokt.auth.dto;

import az.bokt.auth.domain.Permission;
import az.bokt.auth.domain.Role;

import java.util.Set;

public record RoleResponse(
        Long id,
        String name,
        String description,
        Set<Permission> permissions
) {
    public static RoleResponse from(Role r) {
        return new RoleResponse(r.getId(), r.getName(), r.getDescription(), r.getPermissions());
    }
}
