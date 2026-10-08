package az.bokt.credit.payment;

import az.bokt.common.domain.EntityStatus;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.credit.domain.Credit;
import az.bokt.credit.processing.ProcessingClient;
import az.bokt.credit.processing.ProcessingErrorCode;
import az.bokt.credit.processing.ProcessingErrorMapper;
import az.bokt.credit.processing.ProcessingRequest;
import az.bokt.credit.processing.ProcessingResult;
import az.bokt.credit.processing.ReversalRequest;
import az.bokt.tenant.domain.CardType;
import az.bokt.tenant.domain.OrgCard;
import az.bokt.tenant.repo.OrgCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Платёжная цепочка выдачи кредита (валюта — AZN):
 * <ol>
 *   <li>выбираем из пула активную материнскую карту (CHARGE, GUID "CH"+id);</li>
 *   <li>выбираем активную карту выдачи с достаточным остатком лимита (TOPUP, GUID "TP"+id);</li>
 *   <li>уменьшаем остаток карты выдачи; клиент затем снимает наличные с этой карты.</li>
 * </ol>
 * Если TOPUP не прошёл после успешного CHARGE — компенсирующий reversal CHARGE.
 * Лимит проверяется/резервируется ДО процессинга; при отказе транзакция откатывается,
 * возвращая остаток карты. Идемпотентность по GUID (дубликат E010000 = уже выполнено).
 * Остаток материнской карты берётся из процессинга (задел) — здесь пока не списывается.
 */
@Service
public class CreditPaymentService {

    private static final Logger log = LoggerFactory.getLogger(CreditPaymentService.class);
    private static final String AZN = "944";

    private final ProcessingClient processingClient;
    private final OrgCardRepository orgCardRepo;

    public CreditPaymentService(ProcessingClient processingClient, OrgCardRepository orgCardRepo) {
        this.processingClient = processingClient;
        this.orgCardRepo = orgCardRepo;
    }

    /** Провести выдачу. Мутирует credit (GUID/RRN) и остаток карты выдачи. Бросает при отказе. */
    public void settle(Credit credit) {
        long amount = credit.getAmountMinor();

        // 1. Материнская карта (источник)
        OrgCard mother = orgCardRepo
                .findFirstByTypeAndStatusOrderByIdAsc(CardType.MOTHER, EntityStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT,
                        "Нет активной материнской карты"));

        // 2. Карта выдачи с достаточным остатком (строка блокируется для конкурентной безопасности)
        List<OrgCard> payouts = orgCardRepo.findAvailablePayout(amount);
        if (payouts.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "Нет активной карты выдачи с достаточным остатком лимита");
        }
        OrgCard payout = payouts.get(0);

        String chargeGuid = "CH" + credit.getId();
        String topupGuid = "TP" + credit.getId();
        credit.setChargeGuid(chargeGuid);
        credit.setTopupGuid(topupGuid);
        // карта выдачи фиксируется как карта кредита (клиент снимает с неё)
        credit.setClientCardPan(payout.getPan());

        // 3. CHARGE — с материнской
        ProcessingResult chargeResult = processingClient.charge(new ProcessingRequest(
                chargeGuid, mother.getPan(), amount, AZN, chargeGuid, "Credit charge #" + credit.getId()));
        String chargeRrn = switch (chargeResult) {
            case ProcessingResult.Approved a -> a.rrn();
            case ProcessingResult.Declined d -> {
                if (isDuplicate(d)) {
                    log.warn("CHARGE duplicate for credit {} — уже выполнено", credit.getId());
                    yield credit.getChargeRrn();
                }
                throw declineToException(d, "CHARGE");
            }
        };
        credit.setChargeRrn(chargeRrn);

        // 4. TOPUP — на карту выдачи
        ProcessingResult topupResult = processingClient.topup(new ProcessingRequest(
                topupGuid, payout.getPan(), amount, AZN, topupGuid, "Credit topup #" + credit.getId()));
        switch (topupResult) {
            case ProcessingResult.Approved a -> {
                credit.setTopupRrn(a.rrn());
                // резерв лимита карты выдачи
                payout.setBalanceMinor(payout.getBalanceMinor() - amount);
                orgCardRepo.save(payout);
            }
            case ProcessingResult.Declined d -> {
                if (isDuplicate(d)) {
                    log.warn("TOPUP duplicate for credit {} — уже выполнено", credit.getId());
                    return;
                }
                compensateCharge(credit, mother);
                throw declineToException(d, "TOPUP");
            }
        }
    }

    private void compensateCharge(Credit credit, OrgCard mother) {
        if (credit.getChargeRrn() == null) {
            return;
        }
        try {
            ProcessingResult r = processingClient.reverse(new ReversalRequest(
                    credit.getChargeGuid(), credit.getChargeRrn(), credit.getAmountMinor(), AZN));
            log.info("Compensating reversal of CHARGE for credit {}: {}", credit.getId(), r);
        } catch (RuntimeException ex) {
            log.error("Compensating reversal FAILED for credit {} (guid={}, rrn={})",
                    credit.getId(), credit.getChargeGuid(), credit.getChargeRrn(), ex);
        }
    }

    private boolean isDuplicate(ProcessingResult.Declined d) {
        return d.code() == ProcessingErrorCode.DUPLICATE_REFERENCE;
    }

    private BusinessException declineToException(ProcessingResult.Declined d, String op) {
        ErrorCode code = ProcessingErrorMapper.toErrorCode(d.code());
        String msg = op + " отклонён процессингом: " + code.defaultMessage()
                + (d.rawMessage() == null ? "" : " (" + d.rawMessage() + ")");
        return new BusinessException(code, msg);
    }
}
