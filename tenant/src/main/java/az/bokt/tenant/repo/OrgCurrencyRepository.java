package az.bokt.tenant.repo;

import az.bokt.tenant.domain.OrgCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgCurrencyRepository extends JpaRepository<OrgCurrency, Long> {
    List<OrgCurrency> findByCurrencyId(Long currencyId);
}
