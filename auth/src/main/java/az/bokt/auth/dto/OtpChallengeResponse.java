package az.bokt.auth.dto;

/** Ответ шага 1: требуется подтверждение OTP, отправленного по SMS. */
public record OtpChallengeResponse(
        String reference,
        long otpTtlSeconds
) {}
