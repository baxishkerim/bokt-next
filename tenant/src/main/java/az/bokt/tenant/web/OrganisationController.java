package az.bokt.tenant.web;

import az.bokt.tenant.dto.OrganisationResponse;
import az.bokt.tenant.dto.RegisterOrganisationRequest;
import az.bokt.tenant.service.OrganisationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Управление организациями (NBCO). Только супер-админ платформы (TENANT_MANAGE). */
@RestController
@RequestMapping("/api/organisations")
@PreAuthorize("hasAuthority('TENANT_MANAGE')")
public class OrganisationController {

    private final OrganisationService organisationService;

    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @PostMapping
    public OrganisationResponse register(@Valid @RequestBody RegisterOrganisationRequest req) {
        return OrganisationResponse.from(organisationService.register(req));
    }

    @GetMapping
    public List<OrganisationResponse> list() {
        return organisationService.list().stream().map(OrganisationResponse::from).toList();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        organisationService.setActive(id, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        organisationService.setActive(id, false);
        return ResponseEntity.noContent().build();
    }
}
