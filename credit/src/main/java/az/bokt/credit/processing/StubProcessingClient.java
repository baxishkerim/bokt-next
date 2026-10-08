package az.bokt.credit.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Заглушка процессинга. Регистрируется бином в {@link ProcessingConfig} с
 * @ConditionalOnMissingBean — как только появится реальный {@link ProcessingClient},
 * заглушка перестанет создаваться.
 */
public class StubProcessingClient implements ProcessingClient {

    private static final Logger log = LoggerFactory.getLogger(StubProcessingClient.class);
    private static final String NOTE = "ProcessingClient не подключён (заглушка). Реализуйте отправку на процессинг.";

    @Override
    public ProcessingResult charge(ProcessingRequest request) {
        log.warn("[STUB] CHARGE guid={} amount={} -> отказ: {}", request.guid(), request.amountMinor(), NOTE);
        return new ProcessingResult.Declined(ProcessingErrorCode.UNKNOWN, NOTE);
    }

    @Override
    public ProcessingResult topup(ProcessingRequest request) {
        log.warn("[STUB] TOPUP guid={} amount={} -> отказ: {}", request.guid(), request.amountMinor(), NOTE);
        return new ProcessingResult.Declined(ProcessingErrorCode.UNKNOWN, NOTE);
    }

    @Override
    public ProcessingResult reverse(ReversalRequest request) {
        log.warn("[STUB] REVERSE guid={} amount={} -> отказ: {}", request.originalGuid(), request.amountMinor(), NOTE);
        return new ProcessingResult.Declined(ProcessingErrorCode.UNKNOWN, NOTE);
    }
}
