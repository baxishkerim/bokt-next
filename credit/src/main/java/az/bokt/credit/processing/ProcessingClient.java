package az.bokt.credit.processing;

/**
 * ПОРТ процессинга. Приложение зависит только от этого интерфейса; конкретная реализация
 * (SOAP-клиент TranzAxis — {@code TranzaxisProcessingClient}) подключается бином и подменяет
 * заглушку {@code StubProcessingClient}.
 * <p>
 * Выдача кредита — ОДНА операция {@link #charge}: списание с материнской карты на карту выдачи.
 * Все операции идемпотентны по {@code guid} (Match CheckForDuplicate): повторный вызов с тем же
 * ключом возвращает результат исходной операции, а не проводит её ещё раз.
 */
public interface ProcessingClient {

    /** Выдача: материнская карта → карта выдачи (одна операция). */
    ProcessingResult charge(ProcessingRequest request);

    /** Возврат (полный/частичный) ранее проведённой выдачи. */
    ProcessingResult reverse(ReversalRequest request);

    /** Справка по карте (остаток/статус/срок). {@code null}, если карта не найдена. */
    CardInfo getCard(String pan);
}
