package az.bokt.client.web;

import az.bokt.client.dto.ClientResponse;
import az.bokt.client.dto.CreateClientRequest;
import az.bokt.client.service.ClientService;
import az.bokt.common.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENT_MANAGE')")
    public ClientResponse create(@Valid @RequestBody CreateClientRequest req) {
        return ClientResponse.from(clientService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_MANAGE')")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody CreateClientRequest req) {
        return ClientResponse.from(clientService.update(id, req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENT_MANAGE','CREDIT_VIEW','CREDIT_ADD')")
    public ClientResponse get(@PathVariable Long id) {
        return ClientResponse.from(clientService.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CLIENT_MANAGE','CREDIT_VIEW','CREDIT_ADD')")
    public PageResponse<ClientResponse> search(@RequestParam(required = false) String q, Pageable pageable) {
        return PageResponse.from(clientService.search(q, pageable), ClientResponse::from);
    }
}
