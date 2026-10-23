package az.bokt.credit.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Заглушка процессинга. Регистрируется бином в {@link ProcessingConfig} с
 * @ConditionalOnMissingBean — как только включён реальный {@code TranzaxisProcessingClient}
 * (bokt.processing.tranzaxis.enabled=true), заглушка перестаёт создаваться.
 */
public class StubProcessingClient implements ProcessingClient {

    private static final Logger log = LoggerFactory.getLogger(StubProcessingClient.class);
    private static final String NOTE = "ProcessingClient не подключён (заглушка). Включите bokt.processing.tranzaxis.*";

    @Override
    public ProcessingResult charge(ProcessingRequest request) {
        log.warn("[STUB] CHARGE guid={} amount={} -> отказ: {}", request.guid(), request.amountMinor(), NOTE);
        return new ProcessingResult.Declined(ProcessingErrorCode.UNKNOWN, NOTE);
    }

    @Override
    public ProcessingResult reverse(ReversalRequest request) {
        log.warn("[STUB] REVERSE guid={} amount={} -> отказ: {}", request.originalGuid(), request.amountMinor(), NOTE);
        return new ProcessingResult.Declined(ProcessingErrorCode.UNKNOWN, NOTE);
    }

    @Override
    public CardInfo getCard(String pan) {
        log.warn("[STUB] GET-CARD pan=*** -> null: {}", NOTE);
        return null;
    }
}
