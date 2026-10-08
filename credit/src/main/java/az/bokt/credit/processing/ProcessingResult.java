package az.bokt.credit.processing;

/**
 * Результат операции процессинга. Sealed-иерархия — обрабатывается исчерпывающим switch
 * с pattern matching, без «магических строк» в бизнес-коде.
 */
public sealed interface ProcessingResult permits ProcessingResult.Approved, ProcessingResult.Declined {

    /** Успех: получены RRN и код авторизации. */
    record Approved(String rrn, String approvalCode) implements ProcessingResult {}

    /** Отказ: код ошибки процессинга + исходное сырое сообщение. */
    record Declined(ProcessingErrorCode code, String rawMessage) implements ProcessingResult {}
}
