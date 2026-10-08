package az.bokt.common.tenant;

/**
 * Держит филиал текущего пользователя в рамках запроса. Если значение задано —
 * Hibernate-фильтр {@code branchFilter} ограничивает кредиты и клиентов этим филиалом
 * (модератор/оператор видят только свой филиал). Если не задано (директор, супер-админ) —
 * фильтр по филиалу не накладывается, виден весь тенант.
 */
public final class BranchContext {

    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private BranchContext() {}

    public static void setBranchId(Long branchId) {
        HOLDER.set(branchId);
    }

    public static Long getBranchId() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
