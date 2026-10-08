package az.bokt.credit.domain;

/**
 * Статус кредита. Числовые коды соответствуют полю BOKT_CREDITS.Checked старой системы,
 * чтобы сохранить совместимость отчётности и миграции:
 * 0 — новый, 1 — подтверждён, 2 — отменён, 3 — отложен.
 */
public enum CreditStatus {
    NEW(0),
    APPROVED(1),
    CANCELLED(2),
    POSTPONED(3);

    private final int code;

    CreditStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static CreditStatus fromCode(int code) {
        for (CreditStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown credit status code: " + code);
    }
}
