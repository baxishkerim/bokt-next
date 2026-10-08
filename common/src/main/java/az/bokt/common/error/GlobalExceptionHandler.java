package az.bokt.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/** Централизованная обработка ошибок → единое тело {@link ApiError}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorCode code = ex.getErrorCode();
        if (code.status().is5xxServerError()) {
            log.error("Business error {} at {}", code, req.getRequestURI(), ex);
        } else {
            log.warn("Business error {} at {}: {}", code, req.getRequestURI(), ex.getMessage());
        }
        return ResponseEntity.status(code.status())
                .body(ApiError.of(code, ex.getMessage(), req.getRequestURI(), correlationId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        ApiError body = new ApiError(Instant.now(), ErrorCode.VALIDATION_FAILED.status().value(),
                ErrorCode.VALIDATION_FAILED.name(), ErrorCode.VALIDATION_FAILED.defaultMessage(),
                req.getRequestURI(), correlationId(), violations);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.status()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}", req.getRequestURI(), ex);
        return ResponseEntity.status(ErrorCode.INTERNAL.status())
                .body(ApiError.of(ErrorCode.INTERNAL, ErrorCode.INTERNAL.defaultMessage(),
                        req.getRequestURI(), correlationId()));
    }

    private ApiError.FieldViolation toViolation(FieldError fe) {
        return new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage());
    }

    private String correlationId() {
        return MDC.get("correlationId");
    }
}
