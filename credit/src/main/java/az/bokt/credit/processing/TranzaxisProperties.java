package az.bokt.credit.processing;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки процессинга TranzAxis. Боевые значения задаются в конфигурации/окружении.
 * Пока enabled=false — работает заглушка.
 *
 * @param enabled         включить реальный клиент TranzAxis вместо заглушки
 * @param url             endpoint SOAP (txUrl)
 * @param initiatorRid    InitiatorRid
 * @param processorInstId ProcessorInstId
 * @param originatorId    OriginatorInstId
 * @param terminalRid     Rid терминала (Term Rid / merchant)
 * @param chargeKind      значение атрибута Kind для выдачи (charge/paymentCredit)
 * @param reverseKind     значение атрибута Kind для реверса
 */
@ConfigurationProperties(prefix = "bokt.processing.tranzaxis")
public record TranzaxisProperties(
        boolean enabled,
        String url,
        String initiatorRid,
        String processorInstId,
        String originatorId,
        String terminalRid,
        String chargeKind,
        String reverseKind
) {
    public TranzaxisProperties {
        if (chargeKind == null) chargeKind = "Payment";
        if (reverseKind == null) reverseKind = "Payment";
    }
}
