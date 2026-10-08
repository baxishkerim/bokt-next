package az.bokt.tenant.web;

import az.bokt.tenant.dto.BranchRequest;
import az.bokt.tenant.dto.BranchResponse;
import az.bokt.tenant.service.OrganisationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Управление филиалами в рамках своей NBCO. */
@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final OrganisationService organisationService;

    public BranchController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public BranchResponse add(@Valid @RequestBody BranchRequest req) {
        return BranchResponse.from(organisationService.addBranch(req.name(), req.frontId()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<BranchResponse> list() {
        return organisationService.listBranches().stream().map(BranchResponse::from).toList();
    }
}
