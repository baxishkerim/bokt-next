package az.bokt.credit.processing;

/**
 * Коды ответов процессинга. Инкапсулируют строковые E-коды старого BOKT,
 * которые раньше приходилось искать в FAQ вручную:
 * <ul>
 *   <li>E000004 — Invalid parameter [PAN] (карта заблокирована);</li>
 *   <li>E010000 — платёж с такой ссылкой уже существует (дубликат);</li>
 *   <li>E000001 — внутренняя ошибка процессинга;</li>
 *   <li>E010002 — недостаточно средств.</li>
 * </ul>
 */
public enum ProcessingErrorCode {

    CARD_LOCKED("E000004"),
    DUPLICATE_REFERENCE("E010000"),
    INTERNAL("E000001"),
    INSUFFICIENT_FUNDS("E010002"),
    UNKNOWN(null);

    private final String rawCode;

    ProcessingErrorCode(String rawCode) {
        this.rawCode = rawCode;
    }

    public String rawCode() {
        return rawCode;
    }

    /** Сопоставить строковый код процессинга с перечислением. */
    public static ProcessingErrorCode fromRaw(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }
        for (ProcessingErrorCode c : values()) {
            if (raw.equalsIgnoreCase(c.rawCode)) {
                return c;
            }
        }
        return UNKNOWN;
    }
}
