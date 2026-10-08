package az.bokt.common.tenant;

import java.util.Optional;

/**
 * Порт для разрешения тенанта (NBCO) по его короткому логину — используется на входе в систему.
 * Реализуется модулем tenant; лежит в common, чтобы auth не зависел от tenant напрямую
 * (иначе получился бы цикл зависимостей между модулями).
 */
public interface TenantLookup {

    Optional<TenantRef> findByLogin(String login);

    boolean existsAndActive(Long tenantId);

    /** Ссылка на тенанта без раскрытия всей доменной модели organisation. */
    record TenantRef(Long id, String login, String name, boolean active) {}
}
