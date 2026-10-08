package az.bokt.credit.repo;

import az.bokt.credit.domain.Credit;
import az.bokt.credit.domain.CreditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditRepository extends JpaRepository<Credit, Long> {

    Page<Credit> findByStatus(CreditStatus status, Pageable pageable);

    Page<Credit> findByClientId(Long clientId, Pageable pageable);

    /** Отчёт по картам (/credit/byPAN). */
    List<Credit> findByClientCardPan(String clientCardPan);

    /** Загруженные из файла (/credit/loaded). */
    Page<Credit> findByFileId(Long fileId, Pageable pageable);

    /** Архив — завершённые кредиты (/credit/archive). */
    Page<Credit> findByStatusIn(List<CreditStatus> statuses, Pageable pageable);
}
