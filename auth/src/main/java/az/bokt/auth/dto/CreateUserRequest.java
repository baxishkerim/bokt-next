package az.bokt.auth.dto;

import az.bokt.common.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Создание пользователя NBCO (по официальному письму, как в BOKT).
 * Пароль и OTP генерируются на бэкенде и отправляются SMS — оператор их не задаёт вручную.
 */
public record CreateUserRequest(
        @NotBlank String username,
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        Gender gender,
        @NotBlank String phone,
        Long branchId,
        @NotEmpty Set<Long> roleIds
) {}
