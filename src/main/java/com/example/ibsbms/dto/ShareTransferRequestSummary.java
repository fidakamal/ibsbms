package com.example.ibsbms.dto;

import com.example.ibsbms.entity.TransAuth;
import com.example.ibsbms.enums.TransferAuthStatus;
import com.example.ibsbms.enums.TransferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShareTransferRequestSummary {

    private String trId;
    private String transferTypeCode;
    private String transferTypeLabel;
    private String debitReference;
    private String creditReference;
    private BigDecimal shareQuantity;
    private LocalDate trDate;
    private LocalDateTime submittedAt;
    private String particulars;
    private TransferAuthStatus status;
    private String remarks;

    public static ShareTransferRequestSummary fromDebitLeg(TransAuth debitLeg) {
        ShareTransferRequestSummary summary = new ShareTransferRequestSummary();

        summary.trId = debitLeg.getTrId();
        summary.transferTypeCode = debitLeg.getTrCode();

        String label;
        try {
            label = TransferType.fromTrCode(debitLeg.getTrCode()).getLabel();
        } catch (IllegalArgumentException e) {
            label = debitLeg.getTrCode();
        }
        summary.transferTypeLabel = label;

        summary.debitReference = debitLeg.getFolioBo();
        summary.creditReference = debitLeg.getContraAccNo();
        summary.shareQuantity = debitLeg.getDrAmt();
        summary.trDate = debitLeg.getTrDate();
        summary.submittedAt = debitLeg.getModifyDate();
        summary.particulars = debitLeg.getParticular();
        Integer trState = debitLeg.getTrState();
        boolean everReturned = debitLeg.getRemarks() != null && !debitLeg.getRemarks().isBlank();

        if (trState != null
                && trState == TransferAuthStatus.PENDING_CHECKER.getCode()
                && everReturned) {

            summary.status = TransferAuthStatus.RESUBMITTED;

        } else {

            summary.status = TransferAuthStatus.fromCode(trState);
        }
        summary.remarks = debitLeg.getRemarks();

        return summary;
    }

    public String getTrId() { return trId; }
    public String getTransferTypeCode() { return transferTypeCode; }
    public String getTransferTypeLabel() { return transferTypeLabel; }
    public String getDebitReference() { return debitReference; }
    public String getCreditReference() { return creditReference; }
    public BigDecimal getShareQuantity() { return shareQuantity; }
    public LocalDate getTrDate() { return trDate; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public String getParticulars() { return particulars; }
    public TransferAuthStatus getStatus() { return status; }
    public String getRemarks() { return remarks; }
}