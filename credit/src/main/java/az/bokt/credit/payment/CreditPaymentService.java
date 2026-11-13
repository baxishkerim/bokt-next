package az.bokt.credit.payment;

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
 * Карта клиента (карта выдачи) — динамическая: её PAN и срок вводит оператор при создании заявки
 * ({@code credit.clientCardPan}/{@code clientCardExpiry}). Все деньги садятся на эту одну карту;
 * клиент затем снимает с неё наличные. Источник — единственная материнская карта организации.
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

        // Источник — единственная материнская карта организации
        OrgCard mother = orgCardRepo.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "У организации не задана материнская карта"));
        if (!mother.isActive()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Материнская карта организации заблокирована");
        }
        if (mother.getExpiry() == null || mother.getExpiry().isBlank()
                || mother.getCvv() == null || mother.getCvv().isBlank()) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "У материнской карты организации не заданы срок и CVV (нужны для выдачи)");
        }

        String guid = "CH" + credit.getId();
        credit.setChargeGuid(guid);

        // Одна операция cardToCard: материнская карта (PAN+срок+CVV) → карта клиента (PAN)
        ProcessingResult result = processingClient.charge(new ProcessingRequest(
                guid,
                mother.getPan(),
                mother.getExpiry(),
                mother.getCvv(),
                credit.getClientCardPan(),
                amount, AZN,
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
