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
import az.bokt.tenant.domain.OrgCard;
import az.bokt.tenant.repo.OrgCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Выдача кредита (валюта — AZN) — ОДНА операция процессинга (paymentCredit):
 * списание с материнской карты организации на карту клиента, GUID "CH"+id.
 * <p>
 * Карта клиента (карта выдачи) — динамическая: её PAN вводит оператор при создании заявки
 * ({@code credit.clientCardPan}), а не берётся из пула. Все деньги садятся на эту одну карту;
 * клиент затем снимает с неё наличные. Источник (материнская карта) выбирается из пула
 * активных карт типа MOTHER.
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

    /** Провести выдачу. Мутирует credit (GUID/RRN). Бросает при отказе. */
    public void settle(Credit credit) {
        long amount = credit.getAmountMinor();

        if (credit.getClientCardPan() == null || credit.getClientCardPan().isBlank()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Не указана карта клиента для выдачи");
        }

        // Источник — активная материнская карта из пула
        OrgCard mother = orgCardRepo
                .findFirstByStatusOrderByIdAsc(EntityStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "Нет активной карты организации (источника)"));

        String guid = "CH" + credit.getId();
        credit.setChargeGuid(guid);

        // Одна операция: материнская карта → карта клиента
        ProcessingResult result = processingClient.charge(new ProcessingRequest(
                guid,
                mother.getPan(), mother.getExpiry(), null,   // CVV не храним → AuthChecked=true
                credit.getClientCardPan(),
                amount, 0L, AZN,
                guid, "Credit #" + credit.getId()));

        switch (result) {
            case ProcessingResult.Approved a -> credit.setChargeRrn(a.rrn());
            case ProcessingResult.Declined d -> {
                if (d.code() == ProcessingErrorCode.DUPLICATE_REFERENCE) {
                    log.warn("CHARGE duplicate for credit {} — уже выполнено", credit.getId());
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
