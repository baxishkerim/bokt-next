package az.bokt.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Шаг 2 входа: подтверждение OTP. */
public record VerifyOtpRequest(
        @NotBlank String reference,
        @NotBlank String code
) {}
