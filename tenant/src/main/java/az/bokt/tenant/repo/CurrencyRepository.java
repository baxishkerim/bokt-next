package az.bokt.tenant.repo;

import az.bokt.tenant.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByValue(String value);
    Optional<Currency> findByCode(String code);
}
