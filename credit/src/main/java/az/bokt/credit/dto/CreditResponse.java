package az.bokt.credit.dto;

import az.bokt.credit.domain.Credit;
import az.bokt.credit.domain.CreditStatus;
import az.bokt.credit.domain.Money;

import java.math.BigDecimal;
import java.time.Instant;

public record CreditResponse(
        Long id,
        Long clientId,
        String maskedClientCardPan,
        BigDecimal amount,
        Long currencyId,
        CreditStatus status,
        int statusCode,
        Long branchId,
        String description,
        Long fileId,
        String chargeGuid,
        String topupGuid,
        String chargeRrn,
        String topupRrn,
        Instant approvedAt
) {
    public static CreditResponse from(Credit c) {
        return new CreditResponse(
                c.getId(), c.getClientId(), mask(c.getClientCardPan()), Money.toDecimal(c.getAmountMinor()),
                c.getCurrencyId(), c.getStatus(), c.getStatus().code(), c.getBranchId(), c.getDescription(),
                c.getFileId(), c.getChargeGuid(), c.getTopupGuid(), c.getChargeRrn(), c.getTopupRrn(),
                c.getApprovedAt());
    }

    private static String mask(String pan) {
        if (pan == null || pan.length() < 10) {
            return "****";
        }
        return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
    }
}
