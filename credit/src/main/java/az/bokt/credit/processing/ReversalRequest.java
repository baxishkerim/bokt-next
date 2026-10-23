package az.bokt.credit.processing;

/**
 * Запрос на возврат (полный или частичный) ранее проведённой выдачи.
 * Связывается с исходной операцией через Link Key = GUID исходного charge.
 *
 * @param originalGuid  GUID исходной операции ("CH"+creditId) — идёт в Link Key
 * @param originalRrn   RRN исходной операции
 * @param pan           карта, по которой делается реверс (Cust/Token/Card — материнская)
 * @param amountMinor   сумма возврата в минорных единицах
 * @param currencyCode  числовой ISO-код валюты
 */
public record ReversalRequest(
        String originalGuid,
        String originalRrn,
        String pan,
        long amountMinor,
        String currencyCode
) {}
