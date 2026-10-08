package az.bokt.credit.processing;

/**
 * Запрос на возврат (полный или частичный) ранее проведённой операции.
 * Аналог BOKT_SENDREVERSAL_PARTITIAL.
 *
 * @param originalGuid  GUID исходной операции
 * @param originalRrn   RRN исходной операции
 * @param amountMinor   сумма возврата в минорных единицах (для частичного — меньше исходной)
 * @param currencyCode  ISO-код валюты
 */
public record ReversalRequest(
        String originalGuid,
        String originalRrn,
        long amountMinor,
        String currencyCode
) {}
