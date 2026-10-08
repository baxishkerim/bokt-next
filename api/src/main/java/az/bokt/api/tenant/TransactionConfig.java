package az.bokt.api.tenant;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Фиксирует порядок транзакционного advice (order 0) так, чтобы он был ВНЕШНИМ
 * по отношению к {@link TenantFilterAspect} (order 100). Тогда фильтр тенанта
 * включается уже внутри активной транзакции, на её Hibernate-сессии.
 */
@Configuration
@EnableTransactionManagement(order = 0)
public class TransactionConfig {
}
