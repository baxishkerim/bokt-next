package az.bokt.credit.service;

import az.bokt.auth.security.CurrentUser;
import az.bokt.client.domain.Client;
import az.bokt.client.repo.ClientRepository;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.credit.domain.Credit;
import az.bokt.credit.domain.CreditStatus;
import az.bokt.credit.domain.Money;
import az.bokt.credit.dto.CreateCreditRequest;
import az.bokt.credit.payment.CreditPaymentService;
import az.bokt.credit.repo.CreditRepository;
import az.bokt.notification.service.NotificationService;
import az.bokt.tenant.domain.Currency;
import az.bokt.tenant.repo.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Жизненный цикл кредита: создание оператором, подтверждение модератором (с проведением
 * платежа), отмена, отложение. Просмотровые выборки: список, по PAN, архив, загруженные из файла.
 */
@Service
public class CreditService {

    private static final Logger log = LoggerFactory.getLogger(CreditService.class);

    private final CreditRepository creditRepo;
    private final ClientRepository clientRepo;
    private final CurrencyRepository currencyRepo;
    private final CreditPaymentService paymentService;
    private final NotificationService notificationService;

    public CreditService(CreditRepository creditRepo,
                         ClientRepository clientRepo,
                         CurrencyRepository currencyRepo,
                         CreditPaymentService paymentService,
                         NotificationService notificationService) {
        this.creditRepo = creditRepo;
        this.clientRepo = clientRepo;
        this.currencyRepo = currencyRepo;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Credit add(CreateCreditRequest req) {
        Client client = clientRepo.findById(req.clientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "Клиент не найден"));

        Credit credit = new Credit();
        credit.setClientId(client.getId());
        credit.setAmountMinor(Money.toMinor(req.amount()));
        credit.setCurrencyId(req.currencyId());
        credit.setBranchId(req.branchId());
        credit.setSecretWord(req.secretWord());
        credit.setDescription(req.description());
        credit.setStatus(CreditStatus.NEW);
        credit.setCreatedBy(CurrentUser.requireUserId());
        return creditRepo.save(credit);
    }

    /**
     * Подтверждение кредита модератором: проводится платёжная цепочка CHARGE→TOPUP,
     * и только при успехе кредит переходит в APPROVED.
     */
    @Transactional
    public Credit approve(Long creditId) {
        Credit credit = creditRepo.findById(creditId)
                .orElseThrow(() -> BusinessException.notFound("Кредит"));

        if (!credit.isApprovable()) {
            throw new BusinessException(ErrorCode.CREDIT_NOT_APPROVABLE);
        }

        // проведение денег через процессинг (порт); при отказе — BusinessException, транзакция откатится
        paymentService.settle(credit);

        credit.setStatus(CreditStatus.APPROVED);
        credit.setApprovedBy(CurrentUser.requireUserId());
        credit.setApprovedAt(Instant.now());
        Credit saved = creditRepo.save(credit);

        notifyClientIssued(saved);
        return saved;
    }

    @Transactional
    public Credit cancel(Long creditId) {
        Credit credit = creditRepo.findById(creditId)
                .orElseThrow(() -> BusinessException.notFound("Кредит"));
        if (!credit.isCancellable()) {
            throw new BusinessException(ErrorCode.CREDIT_NOT_CANCELLABLE);
        }
        credit.setStatus(CreditStatus.CANCELLED);
        credit.setCancelledBy(CurrentUser.requireUserId());
        return creditRepo.save(credit);
    }

    @Transactional
    public Credit postpone(Long creditId) {
        Credit credit = creditRepo.findById(creditId)
                .orElseThrow(() -> BusinessException.notFound("Кредит"));
        if (credit.getStatus() != CreditStatus.NEW) {
            throw new BusinessException(ErrorCode.CONFLICT, "Отложить можно только новый кредит");
        }
        credit.setStatus(CreditStatus.POSTPONED);
        return creditRepo.save(credit);
    }

    @Transactional(readOnly = true)
    public Credit get(Long creditId) {
        return creditRepo.findById(creditId)
                .orElseThrow(() -> BusinessException.notFound("Кредит"));
    }

    @Transactional(readOnly = true)
    public Page<Credit> list(CreditStatus status, Pageable pageable) {
        return status == null ? creditRepo.findAll(pageable) : creditRepo.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Credit> byPan(String clientCardPan) {
        return creditRepo.findByClientCardPan(clientCardPan);
    }

    @Transactional(readOnly = true)
    public Page<Credit> loaded(Long fileId, Pageable pageable) {
        return creditRepo.findByFileId(fileId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Credit> archive(Pageable pageable) {
        return creditRepo.findByStatusIn(List.of(CreditStatus.APPROVED, CreditStatus.CANCELLED), pageable);
    }

    private void notifyClientIssued(Credit credit) {
        try {
            Client client = clientRepo.findById(credit.getClientId()).orElse(null);
            Currency currency = currencyRepo.findById(credit.getCurrencyId()).orElse(null);
            if (client != null && client.getPhone() != null && currency != null) {
                notificationService.sendCreditIssued(client.getPhone(),
                        Money.toDecimal(credit.getAmountMinor()), currency.getValue(), credit.getSecretWord());
            }
        } catch (RuntimeException ex) {
            log.warn("Не удалось отправить SMS клиенту о выдаче кредита {}", credit.getId(), ex);
        }
    }
}
