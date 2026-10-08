package az.bokt.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Шаг 1 входа: короткий логин NBCO + учётные данные пользователя. */
public record LoginRequest(
        @NotBlank String orgLogin,
        @NotBlank String username,
        @NotBlank String password
) {}
