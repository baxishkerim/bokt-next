package az.bokt.fileimport.service;

import az.bokt.auth.security.CurrentUser;
import az.bokt.client.domain.Client;
import az.bokt.client.repo.ClientRepository;
import az.bokt.common.error.BusinessException;
import az.bokt.common.error.ErrorCode;
import az.bokt.credit.domain.Credit;
import az.bokt.credit.domain.CreditStatus;
import az.bokt.credit.domain.Money;
import az.bokt.credit.repo.CreditRepository;
import az.bokt.fileimport.domain.CreditFile;
import az.bokt.fileimport.repo.CreditFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Импорт кредитов из CSV-файла (замена загрузки через BOKT_FILES).
 * <p>
 * Формат строки: {@code pin;clientCardPan;amount;currencyId;description}
 * (разделитель — точка с запятой; первая строка — заголовок).
 * Кредиты создаются в статусе NEW и привязываются к записи файла; подтверждаются
 * потом штатным потоком approve. Клиент ищется по PIN в рамках тенанта.
 */
@Service
public class FileImportService {

    private static final String DELIMITER = ";";

    private final CreditFileRepository fileRepo;
    private final CreditRepository creditRepo;
    private final ClientRepository clientRepo;

    public FileImportService(CreditFileRepository fileRepo,
                             CreditRepository creditRepo,
                             ClientRepository clientRepo) {
        this.fileRepo = fileRepo;
        this.creditRepo = creditRepo;
        this.clientRepo = clientRepo;
    }

    @Transactional
    public CreditFile importCredits(MultipartFile file) {
        CreditFile record = new CreditFile();
        record.setFileName(file.getOriginalFilename());
        record.setUploadedBy(CurrentUser.requireUserId());
        record = fileRepo.save(record);

        List<String> errors = new ArrayList<>();
        int total = 0;
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                total++;
                try {
                    parseAndCreate(line, record.getId());
                    imported++;
                } catch (RuntimeException ex) {
                    errors.add("Строка " + lineNo + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            record.setStatus(CreditFile.ImportStatus.FAILED);
            record.setErrors("Ошибка чтения файла: " + e.getMessage());
            return fileRepo.save(record);
        }

        record.setTotalRows(total);
        record.setImportedRows(imported);
        record.setFailedRows(total - imported);
        record.setErrors(errors.isEmpty() ? null : String.join("\n", errors).substring(0, Math.min(2000,
                String.join("\n", errors).length())));
        record.setStatus(errors.isEmpty()
                ? CreditFile.ImportStatus.COMPLETED
                : CreditFile.ImportStatus.COMPLETED_WITH_ERRORS);
        return fileRepo.save(record);
    }

    private void parseAndCreate(String line, Long fileId) {
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length < 4) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "недостаточно колонок");
        }
        String pin = parts[0].trim();
        String cardPan = parts[1].trim();
        BigDecimal amount = new BigDecimal(parts[2].trim());
        Long currencyId = Long.valueOf(parts[3].trim());
        String description = parts.length > 4 ? parts[4].trim() : null;

        Client client = clientRepo.findByPin(pin)
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED, "клиент с PIN " + pin + " не найден"));

        Credit credit = new Credit();
        credit.setClientId(client.getId());
        credit.setClientCardPan(cardPan);
        credit.setAmountMinor(Money.toMinor(amount));
        credit.setCurrencyId(currencyId);
        credit.setDescription(description);
        credit.setStatus(CreditStatus.NEW);
        credit.setBranchId(CurrentUser.branchId());
        credit.setFileId(fileId);
        credit.setCreatedBy(CurrentUser.requireUserId());
        creditRepo.save(credit);
    }
}
