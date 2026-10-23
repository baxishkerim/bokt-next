package az.bokt.credit.payment;

import az.bokt.common.domain.EntityStatus;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.credit.domain.Credit;
import az.bokt.credit.domain.CreditStatus;
import az.bokt.credit.processing.ProcessingClient;
import az.bokt.credit.processing.ProcessingErrorMapper;
import az.bokt.credit.processing.ProcessingResult;
import az.bokt.credit.processing.ReversalRequest;
import az.bokt.credit.repo.CreditRepository;
import az.bokt.tenant.domain.Currency;
import az.bokt.tenant.domain.OrgCard;
import az.bokt.tenant.repo.CurrencyRepository;
import az.bokt.tenant.repo.OrgCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Возврат средств по кредиту (полный или частичный). Реверсирует операцию выдачи (charge),
 * связываясь с ней по Link Key = GUID исходной выдачи. Статус/RRN сохраняются — без ручной
 * правки таблиц. Остаток карты выдачи возвращается на сумму реверса.
 */
@Service
public class ReversalService {

    private static final Logger log = LoggerFactory.getLogger(ReversalService.class);

    private final ProcessingClient processingClient;
    private final CreditRepository creditRepo;
    private final CurrencyRepository currencyRepo;
    private final OrgCardRepository orgCardRepo;

    public ReversalService(ProcessingClient processingClient,
                           CreditRepository creditRepo,
                           CurrencyRepository currencyRepo,
                           OrgCardRepository orgCardRepo) {
        this.processingClient = processingClient;
        this.creditRepo = creditRepo;
        this.currencyRepo = currencyRepo;
        this.orgCardRepo = orgCardRepo;
    }

    /**
     * @param creditId    кредит, по которому делается возврат
     * @param amountMinor сумма возврата в минорных единицах; {@code null} — полный возврат
     */
    @Transactional
    public void reverse(Long creditId, Long amountMinor) {
        Credit credit = creditRepo.findById(creditId)
                .orElseThrow(() -> BusinessException.notFound("Кредит"));

        if (credit.getStatus() != CreditStatus.APPROVED || credit.getChargeRrn() == null) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "Возврат возможен только по подтверждённому и выданному кредиту");
        }

        long reverseAmount = amountMinor == null ? credit.getAmountMinor() : amountMinor;
        if (reverseAmount <= 0 || reverseAmount > credit.getAmountMinor()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Некорректная сумма возврата");
        }

        Currency currency = currencyRepo.findById(credit.getCurrencyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "Валюта кредита не найдена"));

        OrgCard mother = orgCardRepo
                .findFirstByStatusOrderByIdAsc(EntityStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "Нет активной карты организации (источника)"));

        ProcessingResult result = processingClient.reverse(new ReversalRequest(
                credit.getChargeGuid(), credit.getChargeRrn(), mother.getPan(), reverseAmount, currency.getCode()));

        switch (result) {
            case ProcessingResult.Approved a ->
                    log.info("Reversal OK credit={} amount={} rrn={}", creditId, reverseAmount, a.rrn());
            case ProcessingResult.Declined d -> {
                ErrorCode code = ProcessingErrorMapper.toErrorCode(d.code());
                throw new BusinessException(code, "Возврат отклонён: " + code.defaultMessage());
            }
        }
    }
}
