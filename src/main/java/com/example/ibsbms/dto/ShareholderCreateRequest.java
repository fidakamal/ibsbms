package com.example.ibsbms.dto;

import jakarta.validation.Valid;

public class ShareholderCreateRequest {

    @Valid
    private BasicInfoDto basicInfo;

    @Valid
    private AddressDto address;

    @Valid
    private BankInfoDto bankInfo;


    public BasicInfoDto getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(BasicInfoDto basicInfo) {
        this.basicInfo = basicInfo;
    }

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public BankInfoDto getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(BankInfoDto bankInfo) {
        this.bankInfo = bankInfo;
    }
}