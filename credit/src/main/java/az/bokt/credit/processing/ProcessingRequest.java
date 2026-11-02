package az.bokt.credit.processing;

/**
 * Запрос на выдачу (charge): одна операция «материнская карта организации → карта клиента».
 * Соответствует методу {@code topUp} старого сервиса (TranzAxis): источник — доверенная карта
 * организации (AuthChecked=true, без CVV и без срока), получатель — карта клиента (PAN + срок).
 *
 * @param guid         идемпотентный ключ операции ("CH"+creditId), идёт в Match Key
 * @param sourcePan    материнская карта организации — источник (Cust/Token/Card Pan)
 * @param payeePan     карта клиента — получатель (Payee/Card Pan)
 * @param payeeExpiry  срок карты клиента в формате "MM/yy"; идёт в ExpDate получателя; может быть null
 * @param amountMinor  сумма в минорных единицах
 * @param currencyCode числовой ISO-код валюты (например "944" для AZN)
 * @param reference    бизнес-ссылка (обычно = guid)
 * @param description  назначение платежа (для логов)
 */
public record ProcessingRequest(
        String guid,
        String sourcePan,
        String payeePan,
        String payeeExpiry,
        long amountMinor,
        String currencyCode,
        String reference,
        String description
) {}
