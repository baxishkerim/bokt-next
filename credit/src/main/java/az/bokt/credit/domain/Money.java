package az.bokt.credit.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Денежные суммы храним и передаём в минорных единицах (копейки/qəpik) как long —
 * это устраняет баг округления из старого BOKT, где сумма 286.40 в процессинге
 * превращалась в 286.39 из-за операций над числами с плавающей точкой.
 * Конвертация в/из BigDecimal — только на границе (ввод/вывод, интеграция).
 */
public final class Money {

    private static final int SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Money() {}

    /** BigDecimal (например 286.40) → минорные единицы (28640). */
    public static long toMinor(BigDecimal amount) {
        return amount.movePointRight(SCALE).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /** Минорные единицы (28640) → BigDecimal с 2 знаками (286.40). */
    public static BigDecimal toDecimal(long minor) {
        return BigDecimal.valueOf(minor).divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }
}
