package az.bokt.auth.repo;

import az.bokt.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByTenantIdAndName(Long tenantId, String name);

    List<Role> findByTenantId(Long tenantId);

    boolean existsByTenantIdAndName(Long tenantId, String name);
}
