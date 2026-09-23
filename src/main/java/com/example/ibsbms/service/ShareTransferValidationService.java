package com.example.ibsbms.service;

import com.example.ibsbms.dto.BoSearchResult;
import com.example.ibsbms.dto.FolioSearchResult;
import com.example.ibsbms.dto.ShareTransferForm;
import com.example.ibsbms.enums.TransferType;
import com.example.ibsbms.exception.ShareTransferValidationException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class ShareTransferValidationService {

    private final AccountSearchService accountSearchService;

    public ShareTransferValidationService(AccountSearchService accountSearchService) {
        this.accountSearchService = accountSearchService;
    }

    public void validateForSubmit(ShareTransferForm form) {

        TransferType type = form.getTransferType();
        if (type == null) {
            throw new ShareTransferValidationException("Transfer type is required.");
        }

        String debitRef = trimOrNull(form.getDebitReference());
        String creditRef = trimOrNull(form.getCreditReference());

        if (debitRef == null || creditRef == null) {
            throw new ShareTransferValidationException(
                    "Debit and credit reference are required.");
        }

        if (debitRef.equalsIgnoreCase(creditRef)) {
            throw new ShareTransferValidationException(
                    "Debit and credit account cannot be the same (" + debitRef + ").");
        }

        BigDecimal quantity = form.getShareQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ShareTransferValidationException(
                    "No. of shares must be greater than zero.");
        }

        if (type.getDebitSide() == TransferType.AccountKind.FOLIO) {
            FolioSearchResult debit = accountSearchService.searchFolio(debitRef);
            validateFolioDebit(debit, quantity);
        } else {
            BoSearchResult debit = accountSearchService.searchBo(debitRef);
            validateBoSide(debit);
        }

        if (type.getCreditSide() == TransferType.AccountKind.FOLIO) {
            FolioSearchResult credit = accountSearchService.searchFolio(creditRef);
            validateFolioCredit(credit);
        } else {
            BoSearchResult credit = accountSearchService.searchBo(creditRef);
            validateBoSide(credit);
        }
    }

    private void validateFolioDebit(FolioSearchResult debit, BigDecimal quantity) {

        if (!debit.isActive()) {
            throw new ShareTransferValidationException(
                    "Debit Folio " + debit.getFolioNo() + " is not active.");
        }

        if (debit.isLien()) {
            throw new ShareTransferValidationException(
                    "Debit Folio " + debit.getFolioNo()
                            + " is under lien; transfer is not allowed.");
        }

        BigDecimal available = debit.getTransferableBalance() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(debit.getTransferableBalance());

        if (quantity.compareTo(available) > 0) {
            throw new ShareTransferValidationException(
                    "Requested quantity (" + quantity
                            + ") exceeds transferable balance (" + available
                            + ") for Folio " + debit.getFolioNo() + ".");
        }
    }

    private void validateFolioCredit(FolioSearchResult credit) {

        if (!credit.isActive()) {
            throw new ShareTransferValidationException(
                    "Credit Folio " + credit.getFolioNo() + " is not active.");
        }

        if (credit.isLien()) {
            throw new ShareTransferValidationException(
                    "Credit Folio " + credit.getFolioNo()
                            + " is under lien; transfer is not allowed.");
        }
    }

    private void validateBoSide(BoSearchResult bo) {
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}