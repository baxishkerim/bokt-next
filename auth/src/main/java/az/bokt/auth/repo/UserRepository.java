package az.bokt.auth.repo;

import az.bokt.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя при входе — по тенанту и логину.
     * tenantId передаётся явно, т.к. на этапе логина Hibernate-фильтр тенанта ещё не активен.
     */
    Optional<User> findByTenantIdAndUsername(Long tenantId, String username);

    boolean existsByTenantIdAndUsername(Long tenantId, String username);

    Page<User> findByTenantId(Long tenantId, Pageable pageable);
}
