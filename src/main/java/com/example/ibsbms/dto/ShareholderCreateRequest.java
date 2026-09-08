package com.example.ibsbms.dto;

import jakarta.validation.Valid;

public class ShareholderCreateRequest {

    @Valid
    private BasicInfoDto basicInfo = new BasicInfoDto();

    @Valid
    private AddressDto address = new AddressDto();

    @Valid
    private BankInfoDto bankInfo = new BankInfoDto();


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