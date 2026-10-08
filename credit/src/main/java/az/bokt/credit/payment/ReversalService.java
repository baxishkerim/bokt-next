package az.bokt.credit.payment;

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
import az.bokt.tenant.repo.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Возврат средств по кредиту (полный или частичный). Реверсируется операция TOPUP
 * (средства, выданные клиенту). Аналог функции BOKT_SENDREVERSAL_PARTITIAL,
 * но с сохранением RRN/GUID и статуса — без ручной правки таблиц.
 */
@Service
public class ReversalService {

    private static final Logger log = LoggerFactory.getLogger(ReversalService.class);

    private final ProcessingClient processingClient;
    private final CreditRepository creditRepo;
    private final CurrencyRepository currencyRepo;

    public ReversalService(ProcessingClient processingClient,
                           CreditRepository creditRepo,
                           CurrencyRepository currencyRepo) {
        this.processingClient = processingClient;
        this.creditRepo = creditRepo;
        this.currencyRepo = currencyRepo;
    }

    /**
     * @param creditId    кредит, по которому делается возврат
     * @param amountMinor сумма возврата в минорных единицах; {@code null} — полный возврат
     */
    @Transactional
    public void reverse(Long creditId, Long amountMinor) {
        Credit credit = creditRepo.findById(creditId)
                .orElseThrow(() -> BusinessException.notFound("Кредит"));

        if (credit.getStatus() != CreditStatus.APPROVED || credit.getTopupRrn() == null) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "Возврат возможен только по подтверждённому и выданному кредиту");
        }

        long reverseAmount = amountMinor == null ? credit.getAmountMinor() : amountMinor;
        if (reverseAmount <= 0 || reverseAmount > credit.getAmountMinor()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Некорректная сумма возврата");
        }

        Currency currency = currencyRepo.findById(credit.getCurrencyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "Валюта кредита не найдена"));

        ProcessingResult result = processingClient.reverse(new ReversalRequest(
                credit.getTopupGuid(), credit.getTopupRrn(), reverseAmount, currency.getCode()));

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
