package az.bokt.credit.processing;

/**
 * Запрос к процессингу на одну операцию (CHARGE или TOPUP).
 *
 * @param guid          уникальный идентификатор операции ("CH"+id / "TP"+id) — идемпотентность
 * @param pan           карта (источник для CHARGE, получатель для TOPUP)
 * @param amountMinor   сумма в минорных единицах
 * @param currencyCode  ISO-код валюты (numeric)
 * @param reference     бизнес-ссылка (обычно тоже guid) — по ней процессинг ловит дубликаты
 * @param description   назначение платежа
 */
public record ProcessingRequest(
        String guid,
        String pan,
        long amountMinor,
        String currencyCode,
        String reference,
        String description
) {}
