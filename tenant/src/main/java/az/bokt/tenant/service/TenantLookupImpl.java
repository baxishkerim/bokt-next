package az.bokt.tenant.service;

import az.bokt.common.tenant.TenantLookup;
import az.bokt.tenant.domain.Organisation;
import az.bokt.tenant.repo.OrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Реализация порта резолва тенанта по логину (используется auth на входе). */
@Service
public class TenantLookupImpl implements TenantLookup {

    private final OrganisationRepository organisationRepo;

    public TenantLookupImpl(OrganisationRepository organisationRepo) {
        this.organisationRepo = organisationRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantRef> findByLogin(String login) {
        return organisationRepo.findByLogin(login)
                .map(o -> new TenantRef(o.getId(), o.getLogin(), o.getName(), o.isActive()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsAndActive(Long tenantId) {
        return organisationRepo.findById(tenantId)
                .map(Organisation::isActive)
                .orElse(false);
    }
}
