package az.bokt.tenant.dto;

import az.bokt.common.domain.EntityStatus;
import az.bokt.tenant.domain.CardType;
import az.bokt.tenant.domain.OrgCard;

import java.math.BigDecimal;

/** Карта организации с маскированным PAN и остатком лимита (в основных единицах). */
public record OrgCardResponse(
        Long id,
        String maskedPan,
        String expiry,
        CardType type,
        EntityStatus status,
        BigDecimal balance,
        String merchant
) {
    public static OrgCardResponse from(OrgCard c) {
        return new OrgCardResponse(
                c.getId(), mask(c.getPan()), c.getExpiry(), c.getType(), c.getStatus(),
                BigDecimal.valueOf(c.getBalanceMinor()).movePointLeft(2), c.getMerchant());
    }

    private static String mask(String pan) {
        if (pan == null || pan.length() < 10) {
            return "****";
        }
        return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
    }
}
