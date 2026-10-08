package az.bokt.auth.dto;

/** Пара токенов после успешного входа/refresh. */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
    public static TokenResponse bearer(String access, String refresh, long expiresIn) {
        return new TokenResponse(access, refresh, "Bearer", expiresIn);
    }
}
