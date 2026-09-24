package com.example.ibsbms.dto;


import com.example.ibsbms.entity.TransAuth;
import com.example.ibsbms.enums.TransferAuthStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShareTransferCheckerSummary(
        String trId,
        String transferType,
        LocalDate businessDate,
        String source,
        String destination,
        BigDecimal quantity,
        String makerId,
        LocalDateTime submittedAt,
        TransferAuthStatus status
) {

    public static ShareTransferCheckerSummary fromDebitLeg(
            TransAuth debitLeg) {

        return new ShareTransferCheckerSummary(
                debitLeg.getTrId(),
                debitLeg.getTrCode(),
                debitLeg.getTrDate(),
                debitLeg.getFolioBo(),
                trim(debitLeg.getContraAccNo()),
                debitLeg.getDrAmt(),
                debitLeg.getMakerId(),
                debitLeg.getModifyDate(),
                TransferAuthStatus.fromCode(debitLeg.getTrState())
        );
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}