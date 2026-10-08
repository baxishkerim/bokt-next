package az.bokt.credit.web;

import az.bokt.credit.domain.CreditStatus;
import az.bokt.credit.dto.CreateCreditRequest;
import az.bokt.credit.dto.CreditResponse;
import az.bokt.credit.service.CreditService;
import az.bokt.common.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREDIT_ADD')")
    public CreditResponse add(@Valid @RequestBody CreateCreditRequest req) {
        return CreditResponse.from(creditService.add(req));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('CREDIT_APPROVE')")
    public CreditResponse approve(@PathVariable Long id) {
        return CreditResponse.from(creditService.approve(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CREDIT_CANCEL')")
    public CreditResponse cancel(@PathVariable Long id) {
        return CreditResponse.from(creditService.cancel(id));
    }

    @PostMapping("/{id}/postpone")
    @PreAuthorize("hasAuthority('CREDIT_ADD')")
    public CreditResponse postpone(@PathVariable Long id) {
        return CreditResponse.from(creditService.postpone(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CREDIT_VIEW')")
    public CreditResponse get(@PathVariable Long id) {
        return CreditResponse.from(creditService.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CREDIT_VIEW')")
    public PageResponse<CreditResponse> list(@RequestParam(required = false) CreditStatus status, Pageable pageable) {
        return PageResponse.from(creditService.list(status, pageable), CreditResponse::from);
    }

    @GetMapping("/by-pan")
    @PreAuthorize("hasAuthority('CREDIT_BY_PAN')")
    public List<CreditResponse> byPan(@RequestParam String pan) {
        return creditService.byPan(pan).stream().map(CreditResponse::from).toList();
    }

    @GetMapping("/loaded")
    @PreAuthorize("hasAuthority('CREDIT_VIEW')")
    public PageResponse<CreditResponse> loaded(@RequestParam Long fileId, Pageable pageable) {
        return PageResponse.from(creditService.loaded(fileId, pageable), CreditResponse::from);
    }

    @GetMapping("/archive")
    @PreAuthorize("hasAuthority('CREDIT_ARCHIVE_VIEW')")
    public PageResponse<CreditResponse> archive(Pageable pageable) {
        return PageResponse.from(creditService.archive(pageable), CreditResponse::from);
    }
}
