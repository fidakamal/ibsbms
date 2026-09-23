package com.example.ibsbms.dto;

import com.example.ibsbms.enums.TransferType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ShareTransferForm {

    @NotNull
    private TransferType transferType;

    @NotBlank
    @Size(max = 16)
    private String debitReference;


    @NotBlank
    @Size(max = 16)
    private String creditReference;

    /*
     * Feeds T_TRANS_SHARE.DR_SHARE / CR_SHARE, confirmed as
     * NUMBER(16,2) in the real Oracle schema (not a plain
     * integer count) - must be BigDecimal, not Long.
     */
    @NotNull
    @Positive
    private BigDecimal shareQuantity;

    @Size(max = 50)
    private String instrumentNo;

    private LocalDate instrumentDate;

    /*
     * Feeds T_TRANS_SHARE.PARTICULARS, confirmed VARCHAR2(800).
     */
    @NotBlank
    @Size(max = 800)
    private String particulars;

    /*
     * Feeds T_CERTIFICATE.CERTI_NO, confirmed VARCHAR2(120).
     */
    @Size(max = 120)
    private String certificateNo;

    /*
     * Feed T_CERTIFICATE.DIST_FROM / DIST_TO, confirmed
     * VARCHAR2(10) in the real schema - not numeric.
     */
    @Size(max = 10)
    private String distinctiveFrom;

    @Size(max = 10)
    private String distinctiveTo;

    public ShareTransferForm() {
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public String getDebitReference() {
        return debitReference;
    }

    public void setDebitReference(String debitReference) {
        this.debitReference = debitReference;
    }

    public String getCreditReference() {
        return creditReference;
    }

    public void setCreditReference(String creditReference) {
        this.creditReference = creditReference;
    }

    public BigDecimal getShareQuantity() {
        return shareQuantity;
    }

    public void setShareQuantity(BigDecimal shareQuantity) {
        this.shareQuantity = shareQuantity;
    }

    public String getInstrumentNo() {
        return instrumentNo;
    }

    public void setInstrumentNo(String instrumentNo) {
        this.instrumentNo = instrumentNo;
    }

    public LocalDate getInstrumentDate() {
        return instrumentDate;
    }

    public void setInstrumentDate(LocalDate instrumentDate) {
        this.instrumentDate = instrumentDate;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getDistinctiveFrom() {
        return distinctiveFrom;
    }

    public void setDistinctiveFrom(String distinctiveFrom) {
        this.distinctiveFrom = distinctiveFrom;
    }

    public String getDistinctiveTo() {
        return distinctiveTo;
    }

    public void setDistinctiveTo(String distinctiveTo) {
        this.distinctiveTo = distinctiveTo;
    }
}