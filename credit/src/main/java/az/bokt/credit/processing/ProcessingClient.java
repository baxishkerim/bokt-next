package az.bokt.credit.processing;

/**
 * ПОРТ процессинга — здесь подключается реальная отправка запросов на посадку денег.
 * <p>
 * Приложение зависит только от этого интерфейса; конкретная реализация (HTTP-клиент к
 * карточному процессингу, формирование GUID/reference, парсинг ответов и E-кодов)
 * подключается отдельным бином и подменяет заглушку {@code StubProcessingClient}.
 * <p>
 * Все методы должны быть идемпотентны по {@code guid}/{@code reference}: повторный вызов
 * с тем же идентификатором обязан вернуть результат исходной операции, а не провести её ещё раз
 * (процессинг сигнализирует дубликат кодом E010000 → {@link ProcessingErrorCode#DUPLICATE_REFERENCE}).
 */
public interface ProcessingClient {

    /** CHARGE — списание средств с основной карты NBCO. */
    ProcessingResult charge(ProcessingRequest request);

    /** TOPUP — зачисление средств на карту клиента. */
    ProcessingResult topup(ProcessingRequest request);

    /** Возврат (полный/частичный) ранее проведённой операции. */
    ProcessingResult reverse(ReversalRequest request);
}
