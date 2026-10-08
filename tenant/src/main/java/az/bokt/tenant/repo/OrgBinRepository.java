package az.bokt.tenant.repo;

import az.bokt.tenant.domain.OrgBin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgBinRepository extends JpaRepository<OrgBin, Long> {
    List<OrgBin> findByBin(String bin);
    boolean existsByBin(String bin);
}
