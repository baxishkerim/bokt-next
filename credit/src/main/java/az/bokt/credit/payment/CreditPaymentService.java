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
import az.bokt.tenant.domain.CardType;
import az.bokt.tenant.domain.OrgCard;
import az.bokt.tenant.repo.OrgCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Выдача кредита (валюта — AZN) — ОДНА операция процессинга (paymentCredit):
 * списание с материнской карты на карту выдачи, GUID "CH"+id. Клиент затем снимает
 * наличные с карты выдачи.
 * <p>
 * Карта выдачи выбирается из пула (активная, с достаточным остатком лимита, строка
 * блокируется для конкурентной безопасности), её остаток уменьшается на сумму выдачи.
 * При отказе процессинга транзакция откатывается — остаток карты возвращается.
 * Идемпотентность — по GUID (дубликат трактуется как уже выполненная операция).
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
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "Нет активной материнской карты"));

        // 2. Карта выдачи с достаточным остатком (строка блокируется)
        List<OrgCard> payouts = orgCardRepo.findAvailablePayout(amount);
        if (payouts.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "Нет активной карты выдачи с достаточным остатком лимита");
        }
        OrgCard payout = payouts.get(0);

        String guid = "CH" + credit.getId();
        credit.setChargeGuid(guid);
        credit.setClientCardPan(payout.getPan());   // карта, с которой клиент снимает

        // 3. Одна операция: материнская → карта выдачи
        ProcessingResult result = processingClient.charge(new ProcessingRequest(
                guid,
                mother.getPan(), mother.getExpiry(), null,   // CVV не храним → AuthChecked=true
                payout.getPan(),
                amount, 0L, AZN,
                guid, "Credit #" + credit.getId()));

        switch (result) {
            case ProcessingResult.Approved a -> {
                credit.setChargeRrn(a.rrn());
                payout.setBalanceMinor(payout.getBalanceMinor() - amount);
                orgCardRepo.save(payout);
            }
            case ProcessingResult.Declined d -> {
                if (d.code() == ProcessingErrorCode.DUPLICATE_REFERENCE) {
                    log.warn("CHARGE duplicate for credit {} — уже выполнено", credit.getId());
                    payout.setBalanceMinor(payout.getBalanceMinor() - amount);
                    orgCardRepo.save(payout);
                    return;
                }
                throw declineToException(d);
            }
        }
    }

    private BusinessException declineToException(ProcessingResult.Declined d) {
        ErrorCode code = ProcessingErrorMapper.toErrorCode(d.code());
        String msg = "Выдача отклонена процессингом: " + code.defaultMessage()
                + (d.rawMessage() == null ? "" : " (" + d.rawMessage() + ")");
        return new BusinessException(code, msg);
    }
}
