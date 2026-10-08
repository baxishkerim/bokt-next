package az.bokt.credit.web;

import az.bokt.credit.domain.Money;
import az.bokt.credit.dto.ReversalRequestDto;
import az.bokt.credit.payment.ReversalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credits")
public class ReversalController {

    private final ReversalService reversalService;

    public ReversalController(ReversalService reversalService) {
        this.reversalService = reversalService;
    }

    /** Полный или частичный возврат по кредиту. */
    @PostMapping("/{id}/reversal")
    @PreAuthorize("hasAuthority('REVERSAL_EXECUTE')")
    public ResponseEntity<Void> reverse(@PathVariable Long id, @Valid @RequestBody ReversalRequestDto req) {
        Long amountMinor = req.amount() == null ? null : Money.toMinor(req.amount());
        reversalService.reverse(id, amountMinor);
        return ResponseEntity.noContent().build();
    }
}
