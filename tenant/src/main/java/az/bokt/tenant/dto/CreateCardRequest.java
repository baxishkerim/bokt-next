package az.bokt.tenant.dto;

import java.math.BigDecimal;

/**
 * Добавление/изменение материнской карты организации (источник средств). Карта одна на
 * организацию: первый вызов создаёт её, последующие — редактируют. При редактировании
 * {@code pan} можно оставить пустым, чтобы не менять номер (в ответе он замаскирован).
 * balance — информационный остаток в основных единицах (факт берётся из процессинга).
 */
public record CreateCardRequest(
        String pan,
        String expiry,
        String merchant,
        BigDecimal balance
) {}
