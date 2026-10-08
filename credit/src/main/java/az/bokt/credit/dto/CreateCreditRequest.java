package az.bokt.credit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Создание заявки на кредит оператором. Карту клиента оператор НЕ указывает:
 * деньги садятся на карту выдачи организации (выбирается при подтверждении), и клиент
 * снимает наличные с неё. Сумма — в основных единицах (например 286.40), внутри конвертируется
 * в минорные. Валюта системы — AZN.
 */
public record CreateCreditRequest(
        @NotNull Long clientId,
        @NotNull @Positive BigDecimal amount,
        @NotNull Long currencyId,
        Long branchId,
        String secretWord,
        String description
) {}
