package com.example.ibsbms.dto;

import com.example.ibsbms.entity.Shareholder;
public class FolioSearchResult {

    private String folioNo;
    private String name;
    private String fatherName;
    private Integer customerType;

    private String phone;


    private boolean active;
    private Integer statusCode;

    private boolean lien;

    private Long currentBalance;
    private Long transferableBalance;

    public static FolioSearchResult from(Shareholder shareholder) {

        FolioSearchResult result = new FolioSearchResult();

        result.folioNo = shareholder.getFolioBo();
        result.name = shareholder.getCustName();
        result.fatherName = shareholder.getFatherName();
        result.customerType = shareholder.getCustType();
        result.phone = shareholder.getPhone();

        result.active = Integer.valueOf(1).equals(shareholder.getIsValid());
        result.statusCode = shareholder.getStatus();

        result.lien = Integer.valueOf(1).equals(shareholder.getIsLien());

        Long balance = shareholder.getBalance();
        result.currentBalance = balance;
        result.transferableBalance = balance;

        return result;
    }

    public String getFolioNo() {
        return folioNo;
    }

    public String getName() {
        return name;
    }

    public String getFatherName() {
        return fatherName;
    }

    public Integer getCustomerType() {
        return customerType;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public boolean isLien() {
        return lien;
    }

    public Long getCurrentBalance() {
        return currentBalance;
    }

    public Long getTransferableBalance() {
        return transferableBalance;
    }
}