package com.example.ibsbms.enums;

public enum TransferAuthStatus {

    PENDING_CHECKER(0),
    APPROVED(1),
    RETURNED_FOR_MODIFICATION(2),
    REJECTED(3);

    private final int code;

    TransferAuthStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TransferAuthStatus fromCode(Integer code) {
        if (code == null) {
            return PENDING_CHECKER;
        }
        for (TransferAuthStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING_CHECKER;
    }
}