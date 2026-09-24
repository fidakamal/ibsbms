package com.example.ibsbms.dto;

public class ReturnedTransferEditView {

    private final String trId;
    private final ShareTransferForm form;
    private final String returnRemarks;

    public ReturnedTransferEditView(String trId, ShareTransferForm form, String returnRemarks) {
        this.trId = trId;
        this.form = form;
        this.returnRemarks = returnRemarks;
    }

    public String getTrId() {
        return trId;
    }

    public ShareTransferForm getForm() {
        return form;
    }

    public String getReturnRemarks() {
        return returnRemarks;
    }
}