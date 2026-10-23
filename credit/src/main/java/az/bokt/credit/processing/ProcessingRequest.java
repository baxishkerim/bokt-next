package az.bokt.credit.processing;

/**
 * Запрос на выдачу (charge/paymentCredit): одна операция «материнская карта → карта выдачи».
 * Соответствует методу {@code payment}/{@code tx.charge} старого сервиса (TranzAxis).
 *
 * @param guid          идемпотентный ключ операции ("CH"+creditId), идёт в Match Key
 * @param payerPan      материнская карта — источник (Cust/Token/Card)
 * @param payerExpiry   срок материнской карты в формате MMyy
 * @param payerCvv      CVV2 материнской карты; обычно {@code null} → AuthChecked=true (CVV не храним)
 * @param payeePan      карта выдачи — получатель (Payee/Card), клиент снимает с неё
 * @param amountMinor   сумма в минорных единицах
 * @param feeMinor      комиссия в минорных единицах (обычно 0)
 * @param currencyCode  числовой ISO-код валюты (например "944" для AZN)
 * @param reference     бизнес-ссылка (обычно = guid)
 * @param description   назначение платежа
 */
public record ProcessingRequest(
        String guid,
        String payerPan,
        String payerExpiry,
        String payerCvv,
        String payeePan,
        long amountMinor,
        long feeMinor,
        String currencyCode,
        String reference,
        String description
) {}
