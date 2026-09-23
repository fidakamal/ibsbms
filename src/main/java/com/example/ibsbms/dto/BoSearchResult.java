package com.example.ibsbms.dto;
import com.example.ibsbms.entity.AccountCdbl;
import java.math.BigDecimal;

public class BoSearchResult {

    private String boNo;
    private String boName;

    private String boStatus;
    private String suspensionStatus;

    private BigDecimal freeBalance;
    private BigDecimal pledgeBalance;
    private BigDecimal lockinBalance;
    private BigDecimal currentBalance;
    private BigDecimal frozenBalance;

    /*
     * ASSUMPTION, NOT CONFIRMED: uses FREE_BALANCE as a
     * conventional stand-in for "transferable balance" (i.e.
     * unencumbered by pledge/lockin/frozen). PDF section 6.2/25
     * explicitly lists "which BO balance/status combination
     * should block transfer" as BUSINESS CONFIRMATION REQUIRED.
     * Do not use this field to allow/block a transfer until that
     * is confirmed - this DTO is for display only.
     */
    private BigDecimal transferableBalance;

    public static BoSearchResult from(AccountCdbl account) {

        BoSearchResult result = new BoSearchResult();

        result.boNo = account.getBoNo();
        result.boName = account.getBoName();

        result.boStatus = account.getBoStatus();
        result.suspensionStatus = account.getSuspensionStatus();

        result.freeBalance = account.getFreeBalance();
        result.pledgeBalance = account.getPledgeBalance();
        result.lockinBalance = account.getLockinBalance();
        result.currentBalance = account.getCurrentBalance();
        result.frozenBalance = account.getFrozenBalance();

        result.transferableBalance = account.getFreeBalance();

        return result;
    }

    public String getBoNo() {
        return boNo;
    }

    public String getBoName() {
        return boName;
    }

    public String getBoStatus() {
        return boStatus;
    }

    public String getSuspensionStatus() {
        return suspensionStatus;
    }

    public BigDecimal getFreeBalance() {
        return freeBalance;
    }

    public BigDecimal getPledgeBalance() {
        return pledgeBalance;
    }

    public BigDecimal getLockinBalance() {
        return lockinBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getFrozenBalance() {
        return frozenBalance;
    }

    public BigDecimal getTransferableBalance() {
        return transferableBalance;
    }
}