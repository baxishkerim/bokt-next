package az.bokt.tenant.repo;

import az.bokt.common.domain.EntityStatus;
import az.bokt.tenant.domain.CardType;
import az.bokt.tenant.domain.OrgCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrgCardRepository extends JpaRepository<OrgCard, Long> {

    List<OrgCard> findAllByOrderByTypeAscIdAsc();

    List<OrgCard> findByTypeAndStatus(CardType type, EntityStatus status);

    /** Первая активная материнская карта (источник CHARGE). */
    Optional<OrgCard> findFirstByTypeAndStatusOrderByIdAsc(CardType type, EntityStatus status);

    /**
     * Активная карта выдачи с достаточным остатком лимита — с пессимистичной блокировкой строки,
     * чтобы два параллельных подтверждения не пробили лимит одной карты.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c from OrgCard c
            where c.type = az.bokt.tenant.domain.CardType.PAYOUT
              and c.status = az.bokt.common.domain.EntityStatus.ACTIVE
              and c.balanceMinor >= :amountMinor
            order by c.id asc
            """)
    List<OrgCard> findAvailablePayout(@Param("amountMinor") long amountMinor);
}
