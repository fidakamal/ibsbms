package com.example.ibsbms.enums;

/*
 * Confirmed mapping (PDF section 5 "Dynamic UI Rules" + the reference
 * screenshot's "Transaction Type Mapping" panel):
 *
 * Transfer Type          | TR_TYPE  | TR_CODE | Settlement
 * -----------------------|----------|---------|--------------------------
 * Folio to Folio         | TRANSFER | F2F     | none
 * Folio to BO (Demat)    | TRANSFER | F2BO    | Folio 1 as credit ledger
 * BO to Folio (Remat)    | TRANSFER | BO2F    | Folio 1 as debit ledger
 */
public enum TransferType {

    FOLIO_TO_FOLIO(
            "F2F",
            "TRANSFER",
            "Folio to Folio",
            AccountKind.FOLIO,
            AccountKind.FOLIO,
            false
    ),

    FOLIO_TO_BO(
            "F2BO",
            "TRANSFER",
            "Folio to BO (Demat)",
            AccountKind.FOLIO,
            AccountKind.BO,
            true
    ),

    BO_TO_FOLIO(
            "BO2F",
            "TRANSFER",
            "BO to Folio (Remat)",
            AccountKind.BO,
            AccountKind.FOLIO,
            true
    );

    /*
     * PDF section 3, confirmed business design decision:
     * "Folio No. 1 is the settlement folio; Demat and Remat posting
     * happens against this account." Never user-selectable
     * (section 5: "Settlement Folio - user cannot manually select/change").
     */
    public static final String SETTLEMENT_FOLIO_BO = "1";

    public enum AccountKind {
        FOLIO,
        BO
    }

    private final String trCode;
    private final String trType;
    private final String label;
    private final AccountKind debitSide;
    private final AccountKind creditSide;
    private final boolean settlementRequired;

    TransferType(
            String trCode,
            String trType,
            String label,
            AccountKind debitSide,
            AccountKind creditSide,
            boolean settlementRequired) {

        this.trCode = trCode;
        this.trType = trType;
        this.label = label;
        this.debitSide = debitSide;
        this.creditSide = creditSide;
        this.settlementRequired = settlementRequired;
    }

    public String getTrCode() {
        return trCode;
    }

    public String getTrType() {
        return trType;
    }

    public String getLabel() {
        return label;
    }

    public AccountKind getDebitSide() {
        return debitSide;
    }

    public AccountKind getCreditSide() {
        return creditSide;
    }

    public boolean isSettlementRequired() {
        return settlementRequired;
    }

    public static TransferType fromTrCode(String trCode) {

        for (TransferType type : values()) {
            if (type.trCode.equalsIgnoreCase(trCode)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Unknown TR_CODE: " + trCode
        );
    }
}