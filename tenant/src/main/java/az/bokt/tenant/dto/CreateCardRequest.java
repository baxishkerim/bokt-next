package az.bokt.tenant.dto;

import java.math.BigDecimal;

/**
 * Добавление/изменение материнской карты организации (источник средств). Карта одна на
 * организацию: первый вызов создаёт её, последующие — редактируют. При редактировании
 * {@code pan}/{@code cvv} можно оставить пустыми, чтобы не менять (в ответе они не возвращаются).
 * balance — информационный остаток в основных единицах (факт берётся из процессинга).
 */
public record CreateCardRequest(
        String pan,
        String expiry,
        String cvv,
        String merchant,
        BigDecimal balance
) {}
