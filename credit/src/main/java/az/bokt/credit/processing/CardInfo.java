package az.bokt.credit.processing;

/**
 * Данные карты из процессинга (операция GetCardByPAN). Используется для остатка
 * материнской карты и валидации карты клиента (статус/срок).
 *
 * @param pan            номер карты
 * @param status         статус карты в процессинге
 * @param expiry         срок действия (MMyy)
 * @param holder         держатель (EmbossName)
 * @param availableMinor доступный остаток в минорных единицах
 * @param currencyCode   код валюты счёта (ISO)
 */
public record CardInfo(
        String pan,
        String status,
        String expiry,
        String holder,
        long availableMinor,
        String currencyCode
) {}
