package az.bokt.auth.dto;

import az.bokt.auth.domain.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank String name,
        String description,
        @NotEmpty Set<Permission> permissions
) {}
