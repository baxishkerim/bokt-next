package az.bokt.auth.dto;

import az.bokt.auth.domain.User;
import az.bokt.common.domain.EntityStatus;
import az.bokt.common.domain.Gender;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String middleName,
        Gender gender,
        String phone,
        EntityStatus status,
        Long branchId,
        LocalDate passwordExpiry,
        boolean superAdmin,
        Set<Long> roleIds
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(), u.getMiddleName(),
                u.getGender(), u.getPhone(), u.getStatus(), u.getBranchId(), u.getPasswordExpiry(),
                u.isSuperAdmin(),
                u.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet()));
    }
}
