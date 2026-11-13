package az.bokt.tenant.service;

import az.bokt.common.domain.EntityStatus;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.common.tenant.TenantContext;
import az.bokt.tenant.domain.OrgCard;
import az.bokt.tenant.dto.CreateCardRequest;
import az.bokt.tenant.repo.OrgCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Управление картами организации (источник средств), включая блокировку.
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
        return cardRepo.findAllByOrderByIdAsc();
    }

    /**
     * Создать или изменить материнскую карту организации (она одна на организацию).
     * Первый вызов создаёт карту, последующие — редактируют существующую. При редактировании
     * пустой {@code pan} означает «не менять номер». Доступно только пользователю организации
     * (директору): супер-админ платформы к тенанту не привязан.
     */
    @Transactional
    public OrgCard add(CreateCardRequest req) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            // Супер-админ платформы не привязан к организации — картами управляет директор.
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Управление картами доступно пользователю организации (директору), а не супер-админу платформы");
        }
        OrgCard card = cardRepo.findFirstByOrderByIdAsc().orElse(null);
        boolean creating = (card == null);
        if (creating) {
            card = new OrgCard();
            card.setTenantId(tenantId);
            card.setStatus(EntityStatus.ACTIVE);
            card.setBalanceMinor(toMinor(req.balance()));
        }
        if (req.pan() != null && !req.pan().isBlank()) {
            card.setPan(req.pan().trim());
        } else if (creating) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Укажите номер карты");
        }
        // CVV: при редактировании пустое значение означает «не менять»
        if (req.cvv() != null && !req.cvv().isBlank()) {
            card.setCvv(req.cvv().trim());
        }
        card.setExpiry(req.expiry());
        card.setMerchant(req.merchant());
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
