package az.bokt.tenant.dto;

import jakarta.validation.constraints.NotBlank;

public record BranchRequest(@NotBlank String name, String frontId) {}
