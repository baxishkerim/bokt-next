package az.bokt.client.service;

import az.bokt.auth.security.CurrentUser;
import az.bokt.client.domain.Client;
import az.bokt.client.dto.CreateClientRequest;
import az.bokt.client.repo.ClientRepository;
import az.bokt.common.domain.Gender;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ведение клиентов NBCO. Изоляция по тенанту обеспечивается Hibernate-фильтром. */
@Service
public class ClientService {

    private final ClientRepository clientRepo;

    public ClientService(ClientRepository clientRepo) {
        this.clientRepo = clientRepo;
    }

    @Transactional
    public Client create(CreateClientRequest req) {
        if (clientRepo.existsByPin(req.pin())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Клиент с таким PIN уже существует");
        }
        Client c = new Client();
        applyFields(c, req);
        c.setCreatedBy(CurrentUser.requireUserId());
        c.setModifiedBy(CurrentUser.requireUserId());
        return clientRepo.save(c);
    }

    @Transactional
    public Client update(Long id, CreateClientRequest req) {
        Client c = clientRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Клиент"));
        applyFields(c, req);
        c.setModifiedBy(CurrentUser.requireUserId());
        return clientRepo.save(c);
    }

    @Transactional(readOnly = true)
    public Client get(Long id) {
        return clientRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Клиент"));
    }

    @Transactional(readOnly = true)
    public Page<Client> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return clientRepo.findAll(pageable);
        }
        return clientRepo.search(q.trim(), pageable);
    }

    private void applyFields(Client c, CreateClientRequest req) {
        c.setPin(req.pin());
        c.setFirstName(req.firstName());
        c.setLastName(req.lastName());
        c.setMiddleName(req.middleName());
        c.setBirthDate(req.birthDate());
        c.setGender(req.gender() == null ? Gender.UNKNOWN : req.gender());
        c.setPhone(req.phone());
        c.setAddress(req.address());
        c.setSecretWord(req.secretWord());
    }
}
