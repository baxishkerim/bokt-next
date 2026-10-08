package az.bokt.tenant.web;

import az.bokt.common.domain.EntityStatus;
import az.bokt.tenant.dto.CreateCardRequest;
import az.bokt.tenant.dto.OrgCardResponse;
import az.bokt.tenant.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Карты организации: материнские и выдачи. Право CARD_MANAGE. */
@RestController
@RequestMapping("/api/cards")
@PreAuthorize("hasAuthority('CARD_MANAGE')")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<OrgCardResponse> list() {
        return cardService.list().stream().map(OrgCardResponse::from).toList();
    }

    @PostMapping
    public OrgCardResponse add(@Valid @RequestBody CreateCardRequest req) {
        return OrgCardResponse.from(cardService.add(req));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(@PathVariable Long id) {
        cardService.setStatus(id, EntityStatus.DISABLED);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long id) {
        cardService.setStatus(id, EntityStatus.ACTIVE);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/balance")
    public OrgCardResponse setBalance(@PathVariable Long id, @RequestBody BigDecimal balance) {
        return OrgCardResponse.from(cardService.setBalance(id, balance));
    }
}
