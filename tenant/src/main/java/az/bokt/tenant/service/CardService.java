package az.bokt.tenant.service;

import az.bokt.common.domain.EntityStatus;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.tenant.domain.OrgCard;
import az.bokt.tenant.dto.CreateCardRequest;
import az.bokt.tenant.repo.OrgCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Управление картами организации (материнские / выдачи), включая блокировку.
 * Работает в контексте своего тенанта — tenant_id проставляется/фильтруется автоматически.
 */
@Service
public class CardService {

    private final OrgCardRepository cardRepo;

    public CardService(OrgCardRepository cardRepo) {
        this.cardRepo = cardRepo;
    }

    @Transactional(readOnly = true)
    public List<OrgCard> list() {
        return cardRepo.findAllByOrderByTypeAscIdAsc();
    }

    @Transactional
    public OrgCard add(CreateCardRequest req) {
        OrgCard card = new OrgCard();
        card.setType(req.type());
        card.setPan(req.pan().trim());
        card.setExpiry(req.expiry());
        card.setMerchant(req.merchant());
        card.setStatus(EntityStatus.ACTIVE);
        card.setBalanceMinor(toMinor(req.balance()));
        return cardRepo.save(card);
    }

    @Transactional
    public void setStatus(Long cardId, EntityStatus status) {
        OrgCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> BusinessException.notFound("Карта"));
        card.setStatus(status);
        cardRepo.save(card);
    }

    /** Установить остаток/лимит карты (в основных единицах). */
    @Transactional
    public OrgCard setBalance(Long cardId, BigDecimal balance) {
        OrgCard card = cardRepo.findById(cardId)
                .orElseThrow(() -> BusinessException.notFound("Карта"));
        if (balance == null || balance.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Некорректный остаток");
        }
        card.setBalanceMinor(toMinor(balance));
        return cardRepo.save(card);
    }

    private long toMinor(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
