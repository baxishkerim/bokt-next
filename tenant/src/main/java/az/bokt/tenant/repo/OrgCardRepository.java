package az.bokt.tenant.repo;

import az.bokt.common.domain.EntityStatus;
import az.bokt.tenant.domain.OrgCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgCardRepository extends JpaRepository<OrgCard, Long> {

    List<OrgCard> findAllByOrderByIdAsc();

    /** Первая активная карта организации — источник CHARGE при выдаче. */
    Optional<OrgCard> findFirstByStatusOrderByIdAsc(EntityStatus status);
}
