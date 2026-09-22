package com.example.ibsbms.dto;

import com.example.ibsbms.enums.TransferType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

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

    @NotNull
    @Positive
    private Long shareQuantity;

    @Size(max = 50)
    private String instrumentNo;

    private LocalDate instrumentDate;
    @NotBlank
    @Size(max = 200)
    private String particulars;

    @Size(max = 30)
    private String certificateNo;

    @PositiveOrZero
    private Long distinctiveFrom;

    @PositiveOrZero
    private Long distinctiveTo;

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

    public Long getShareQuantity() {
        return shareQuantity;
    }

    public void setShareQuantity(Long shareQuantity) {
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

    public Long getDistinctiveFrom() {
        return distinctiveFrom;
    }

    public void setDistinctiveFrom(Long distinctiveFrom) {
        this.distinctiveFrom = distinctiveFrom;
    }

    public Long getDistinctiveTo() {
        return distinctiveTo;
    }

    public void setDistinctiveTo(Long distinctiveTo) {
        this.distinctiveTo = distinctiveTo;
    }
}