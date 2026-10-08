package az.bokt.auth.domain;

/**
 * Права доступа как бизнес-действия, а не URL (в BOKT_PERMISSIONS права были привязаны
 * к путям /credit/approve, /card/list и т.д. и заводились вручную для каждой NBCO).
 * Теперь право привязано к действию и проверяется через @PreAuthorize("hasAuthority(...)").
 */
public enum Permission {

    // Кредиты
    CREDIT_ADD,            // /credit/add — создание заявки (оператор)
    CREDIT_APPROVE,        // /credit/approve — подтверждение (модератор)
    CREDIT_CANCEL,         // отмена кредита
    CREDIT_VIEW,           // /credit/list, /credit/loaded — просмотр
    CREDIT_ARCHIVE_VIEW,   // /credit/archive — архив (отчёт)
    CREDIT_BY_PAN,         // /credit/byPAN — отчёт по картам

    // Карты
    CARD_LIST,             // /card/list — карты клиента
    CARD_TRANSACTIONS,     // /card/transactions — транзакции по карте

    // Справочники / клиенты
    CLIENT_MANAGE,         // ведение клиентов NBCO

    // Импорт и возвраты
    FILE_IMPORT,           // загрузка кредитов из файла
    REVERSAL_EXECUTE,      // выполнение reversal-запросов

    // Карты организации
    CARD_MANAGE,           // управление картами (материнская/выдачи), блокировка

    // Отчёты
    REPORT_VIEW,

    // Администрирование внутри тенанта
    USER_MANAGE,           // управление пользователями своей NBCO
    ROLE_MANAGE,           // создание ролей и назначение прав

    // Администрирование платформы (только супер-админ)
    TENANT_MANAGE;         // регистрация/настройка NBCO

    /** Имя authority в Spring Security. */
    public String authority() {
        return name();
    }
}
