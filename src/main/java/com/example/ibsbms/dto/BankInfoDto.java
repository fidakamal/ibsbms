package com.example.ibsbms.dto;

import jakarta.validation.constraints.Size;

public class BankInfoDto {

    @Size(max = 17)
    private String accNo;

    @Size(max = 80)
    private String bankName;

    @Size(max = 80)
    private String branchName;

    @Size(max = 20)
    private String routingNo;


    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getRoutingNo() {
        return routingNo;
    }

    public void setRoutingNo(String routingNo) {
        this.routingNo = routingNo;
    }
}