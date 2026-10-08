package az.bokt.tenant.web;

import az.bokt.tenant.dto.BranchResponse;
import az.bokt.tenant.dto.BranchWithModeratorResponse;
import az.bokt.tenant.dto.CreateBranchWithModeratorRequest;
import az.bokt.tenant.service.OrganisationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Управление филиалами в рамках своей NBCO. Создаёт директор (USER_MANAGE). */
@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final OrganisationService organisationService;

    public BranchController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    /** Создать филиал вместе с его модератором (начальником филиала). */
    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public BranchWithModeratorResponse add(@Valid @RequestBody CreateBranchWithModeratorRequest req) {
        return BranchWithModeratorResponse.from(organisationService.addBranchWithModerator(req));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<BranchResponse> list() {
        return organisationService.listBranches().stream().map(BranchResponse::from).toList();
    }
}
