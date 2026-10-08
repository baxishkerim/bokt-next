package az.bokt.fileimport.repo;

import az.bokt.fileimport.domain.CreditFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditFileRepository extends JpaRepository<CreditFile, Long> {
    Page<CreditFile> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
