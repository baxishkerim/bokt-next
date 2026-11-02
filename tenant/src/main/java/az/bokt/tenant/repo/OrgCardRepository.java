package az.bokt.tenant.repo;

import az.bokt.tenant.domain.OrgCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgCardRepository extends JpaRepository<OrgCard, Long> {

    List<OrgCard> findAllByOrderByIdAsc();

    /** Единственная материнская карта организации (одна на организацию) — источник CHARGE при выдаче. */
    Optional<OrgCard> findFirstByOrderByIdAsc();
}
