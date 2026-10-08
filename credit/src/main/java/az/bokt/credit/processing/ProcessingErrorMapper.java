package az.bokt.credit.processing;

import az.bokt.common.error.ErrorCode;

/** Перевод кода процессинга в доменный код ошибки. Исчерпывающий switch — новые коды не забудутся. */
public final class ProcessingErrorMapper {

    private ProcessingErrorMapper() {}

    public static ErrorCode toErrorCode(ProcessingErrorCode code) {
        return switch (code) {
            case CARD_LOCKED -> ErrorCode.PROC_CARD_LOCKED;
            case DUPLICATE_REFERENCE -> ErrorCode.PROC_DUPLICATE_REFERENCE;
            case INTERNAL -> ErrorCode.PROC_INTERNAL;
            case INSUFFICIENT_FUNDS -> ErrorCode.PROC_INSUFFICIENT_FUNDS;
            case UNKNOWN -> ErrorCode.PROC_UNKNOWN;
        };
    }
}
