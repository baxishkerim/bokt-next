package az.bokt.common.error;

import java.time.Instant;
import java.util.List;

/** Стандартное тело ответа при ошибке. */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        List<FieldViolation> violations
) {
    public record FieldViolation(String field, String message) {}

    public static ApiError of(ErrorCode code, String message, String path, String correlationId) {
        return new ApiError(Instant.now(), code.status().value(), code.name(), message, path, correlationId, List.of());
    }
}
