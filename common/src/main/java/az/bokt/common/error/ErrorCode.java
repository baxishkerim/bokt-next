package az.bokt.common.error;

import org.springframework.http.HttpStatus;

/**
 * Единый каталог ошибок системы: и внутренние бизнес-ошибки, и коды процессинга
 * из старого BOKT (E000004, E010000, E000001, E010002), которые раньше искали
 * вручную по FAQ. Теперь на них можно матчиться через switch с исчерпывающими ветками.
 */
public enum ErrorCode {

    // --- общие ---
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Некорректные данные запроса"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Объект не найден"),
    CONFLICT(HttpStatus.CONFLICT, "Конфликт состояния"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Недостаточно прав"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Требуется аутентификация"),
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка"),

    // --- аутентификация ---
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"),
    OTP_INVALID(HttpStatus.UNAUTHORIZED, "Неверный или истёкший OTP"),
    OTP_REQUIRED(HttpStatus.UNAUTHORIZED, "Требуется подтверждение OTP"),
    PASSWORD_EXPIRED(HttpStatus.FORBIDDEN, "Срок действия пароля истёк"),
    USER_DISABLED(HttpStatus.FORBIDDEN, "Учётная запись отключена"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Недействительный refresh-токен"),

    // --- кредит ---
    CREDIT_NOT_APPROVABLE(HttpStatus.CONFLICT, "Кредит нельзя подтвердить в текущем статусе"),
    CREDIT_ALREADY_FINALIZED(HttpStatus.CONFLICT, "Кредит уже завершён"),
    CREDIT_NOT_CANCELLABLE(HttpStatus.CONFLICT, "Кредит нельзя отменить в текущем статусе"),

    // --- процессинг (маппинг старых E-кодов BOKT) ---
    PROC_CARD_LOCKED(HttpStatus.UNPROCESSABLE_ENTITY, "Карта заблокирована (E000004: Invalid parameter [PAN])"),
    PROC_DUPLICATE_REFERENCE(HttpStatus.CONFLICT, "Платёж с такой ссылкой уже существует (E010000)"),
    PROC_INTERNAL(HttpStatus.BAD_GATEWAY, "Внутренняя ошибка процессинга (E000001)"),
    PROC_INSUFFICIENT_FUNDS(HttpStatus.UNPROCESSABLE_ENTITY, "Недостаточно средств на основной карте (E010002)"),
    PROC_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "Процессинг недоступен"),
    PROC_UNKNOWN(HttpStatus.BAD_GATEWAY, "Неизвестная ошибка процессинга");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
