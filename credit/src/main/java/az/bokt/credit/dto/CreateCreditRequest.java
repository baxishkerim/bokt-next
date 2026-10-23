package az.bokt.credit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Создание заявки на кредит оператором. Карта клиента (карта выдачи) вводится здесь —
 * она динамическая, одна на заявку; все деньги садятся на неё при подтверждении
 * (списанием с материнской карты организации). Сумма — в основных единицах
 * (например 286.40), внутри конвертируется в минорные. Валюта системы — AZN.
 */
public record CreateCreditRequest(
        @NotNull Long clientId,
        @NotBlank String clientCardPan,
        @NotNull @Positive BigDecimal amount,
        @NotNull Long currencyId,
        Long branchId,
        String secretWord,
        String description
) {}
