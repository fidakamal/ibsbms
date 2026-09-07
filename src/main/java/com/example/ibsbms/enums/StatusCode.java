package com.example.ibsbms.enums;

public enum StatusCode {

    PENDING_CHECKER(1),
    PENDING_APPROVER(2),
    RETURNED_FOR_MODIFICATION(3),
    APPROVED(4),
    REJECTED(5),
    CANCELLED(6);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StatusCode fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (StatusCode status : values()) {
            if (status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Unknown status code: " + code
        );
    }
}

