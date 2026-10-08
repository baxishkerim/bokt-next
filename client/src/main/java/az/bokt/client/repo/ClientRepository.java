package az.bokt.client.repo;

import az.bokt.client.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByPin(String pin);

    boolean existsByPin(String pin);

    @Query("""
            select c from Client c
            where lower(c.lastName) like lower(concat('%', :q, '%'))
               or c.phone like concat('%', :q, '%')
               or c.pin like concat('%', :q, '%')
            """)
    Page<Client> search(@Param("q") String q, Pageable pageable);
}
