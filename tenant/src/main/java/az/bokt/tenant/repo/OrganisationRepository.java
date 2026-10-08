package az.bokt.tenant.repo;

import az.bokt.tenant.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    Optional<Organisation> findByLogin(String login);

    boolean existsByLogin(String login);
}
