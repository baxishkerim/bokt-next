package az.bokt.common.tenant;

/**
 * Держит текущего тенанта (кредитную организацию / NBCO) в рамках обработки одного запроса.
 * <p>
 * Заменяет модель realm-per-tenant из Keycloak: изоляция данных теперь обеспечивается
 * значением tenantId, которое кладётся сюда из JWT в {@code JwtAuthenticationFilter},
 * и Hibernate-фильтром {@code tenantFilter}, накладываемым на все tenant-scoped сущности.
 * <p>
 * Флаг {@code crossTenant} поднимается для супер-администратора платформы
 * (требование ЦБ — доступ ко всем данным всех тенантов): в этом режиме
 * tenant-фильтр не включается.
 */
public final class TenantContext {

    private record State(Long tenantId, boolean crossTenant) {}

    private static final ThreadLocal<State> HOLDER = new ThreadLocal<>();

    private TenantContext() {}

    /** Установить обычного тенанта (сотрудник конкретной NBCO). */
    public static void setTenantId(Long tenantId) {
        HOLDER.set(new State(tenantId, false));
    }

    /** Включить кросс-тенантный режим (супер-админ платформы видит все данные). */
    public static void setCrossTenant() {
        HOLDER.set(new State(null, true));
    }

    /** Текущий tenantId либо {@code null}, если контекст не установлен или это супер-админ. */
    public static Long getTenantId() {
        State s = HOLDER.get();
        return s == null ? null : s.tenantId();
    }

    /** true — фильтр по тенанту накладывать не нужно (супер-админ). */
    public static boolean isCrossTenant() {
        State s = HOLDER.get();
        return s != null && s.crossTenant();
    }

    /** Требуется реальный tenantId (для операторских действий). Бросает, если его нет. */
    public static Long requireTenantId() {
        Long id = getTenantId();
        if (id == null) {
            throw new IllegalStateException("Tenant context is not set for the current request");
        }
        return id;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
