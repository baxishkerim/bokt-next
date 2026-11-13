package az.bokt.credit.processing;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки процессинга TranzAxis. Боевые значения задаются в конфигурации/окружении.
 * Пока enabled=false — работает заглушка.
 *
 * @param enabled          включить реальный клиент TranzAxis вместо заглушки
 * @param url              endpoint SOAP (txUrl)
 * @param initiatorRid     InitiatorRid
 * @param processorInstId  ProcessorInstId
 * @param originatorId     OriginatorInstId (в cardToCard не используется, оставлено для совместимости)
 * @param terminalId       Id терминала (Term Id); если задан — имеет приоритет над terminalRid
 * @param terminalRid      Rid терминала (Term Rid)
 * @param terminalName     имя мерчанта; если задано — добавляется блок Classification (MerchantRid/MerchantName)
 * @param mcc              MCC терминала (Term/Owner Mcc)
 * @param categoryCode     CategoryCode запроса (например "BankCharge")
 * @param chargeKind       значение атрибута Kind для выдачи (по умолчанию "Payment")
 * @param reverseKind      значение атрибута Kind для реверса
 * @param connectTimeoutMs таймаут установки соединения, мс (по умолчанию 5000)
 * @param readTimeoutMs    таймаут ожидания ответа, мс (по умолчанию 15000)
 */
@ConfigurationProperties(prefix = "bokt.processing.tranzaxis")
public record TranzaxisProperties(
        boolean enabled,
        String url,
        String initiatorRid,
        String processorInstId,
        String originatorId,
        Long terminalId,
        String terminalRid,
        String terminalName,
        String mcc,
        String categoryCode,
        String chargeKind,
        String reverseKind,
        Integer connectTimeoutMs,
        Integer readTimeoutMs
) {
    public TranzaxisProperties {
        if (chargeKind == null) chargeKind = "Payment";
        if (reverseKind == null) reverseKind = "Payment";
        if (categoryCode == null) categoryCode = "BankCharge";
        if (connectTimeoutMs == null) connectTimeoutMs = 5000;
        if (readTimeoutMs == null) readTimeoutMs = 15000;
    }
}
