package az.bokt.auth.web;

import az.bokt.common.error.ApiError;
import az.bokt.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Обработка отказов авторизации из method-security (@PreAuthorize).
 * Вынесено в модуль auth, а не в common: класс {@link AccessDeniedException}
 * живёт в spring-security, от которого common сознательно не зависит.
 * <p>
 * Отказы на уровне цепочки фильтров обрабатывает accessDeniedHandler в SecurityConfig;
 * этот advice ловит исключения, вылетающие из проверок прав на методах контроллеров.
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.status())
                .body(ApiError.of(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.defaultMessage(),
                        req.getRequestURI(), MDC.get("correlationId")));
    }
}
