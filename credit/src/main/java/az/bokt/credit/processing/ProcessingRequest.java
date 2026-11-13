package az.bokt.credit.processing;

/**
 * Запрос на выдачу (charge): одна операция «материнская карта организации → карта клиента»,
 * построенная по методу {@code cardToCard} сервиса EBWS (TranzAxis, Kind="Payment").
 * Источник — материнская карта (PAN + срок + CVV), получатель — карта клиента (только PAN).
 *
 * @param guid         идемпотентный ключ операции ("CH"+creditId), идёт в Match Key
 * @param sourcePan    материнская карта организации — источник (Cust/Token/Card Pan)
 * @param sourceExpiry срок материнской карты в формате "MM/yy" (идёт в ExpDate источника)
 * @param sourceCvv    CVV2 материнской карты (идёт в Cust/.../Card/Auth Cvv2)
 * @param payeePan     карта клиента — получатель (Payee/Card Pan)
 * @param amountMinor  сумма в минорных единицах
 * @param currencyCode числовой ISO-код валюты (например "944" для AZN)
 * @param reference    бизнес-ссылка (обычно = guid)
 * @param description  назначение платежа (идёт в TextMess)
 */
public record ProcessingRequest(
        String guid,
        String sourcePan,
        String sourceExpiry,
        String sourceCvv,
        String payeePan,
        long amountMinor,
        String currencyCode,
        String reference,
        String description
) {}
