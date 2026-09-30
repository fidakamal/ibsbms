package com.example.ibsbms.service;

import com.example.ibsbms.dto.*;
import com.example.ibsbms.entity.*;
import com.example.ibsbms.enums.TransferAuthStatus;
import com.example.ibsbms.enums.TransferType;
import com.example.ibsbms.exception.ShareTransferValidationException;
import com.example.ibsbms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


import java.util.ArrayList;
import java.util.Comparator;




@Service
public class ShareTransferWorkflowService {

    private static final int PARTICULAR_MAX_LENGTH = 255;

    private final ShareTransferValidationService validationService;
    private final TransAuthRepository transAuthRepository;
    private final WorkflowIdService workflowIdService;
    private final BusinessDateService businessDateService;

    private final ShareholderRepository shareholderRepository;
    private final TransShareRepository transShareRepository;
    private final BusinessAuditRepository businessAuditRepository;

    private final ShareMovementRepository shareMovementRepository;
    private final CdblOutBatchRepository cdblOutBatchRepository;
    private final CdblOutItemRepository cdblOutItemRepository;

    private final AccountCdblRepository accountCdblRepository;

    public ShareTransferWorkflowService(
            ShareTransferValidationService validationService,
            TransAuthRepository transAuthRepository,
            WorkflowIdService workflowIdService,
            BusinessDateService businessDateService,
            ShareholderRepository shareholderRepository,
            TransShareRepository transShareRepository,
            BusinessAuditRepository businessAuditRepository,
            ShareMovementRepository shareMovementRepository,
            CdblOutBatchRepository cdblOutBatchRepository,
            CdblOutItemRepository cdblOutItemRepository,
            AccountCdblRepository accountCdblRepository) {

        this.validationService = validationService;
        this.transAuthRepository = transAuthRepository;
        this.workflowIdService = workflowIdService;
        this.businessDateService = businessDateService;
        this.shareholderRepository = shareholderRepository;
        this.transShareRepository = transShareRepository;
        this.businessAuditRepository = businessAuditRepository;
        this.shareMovementRepository = shareMovementRepository;
        this.cdblOutBatchRepository = cdblOutBatchRepository;
        this.cdblOutItemRepository = cdblOutItemRepository;
        this.accountCdblRepository = accountCdblRepository;
    }

    @Transactional
    public String submitForApproval(ShareTransferForm form, String makerId, String makerIp) {

        validationService.validateForSubmit(form);

        String particular = form.getParticulars();

        if (particular != null && particular.length() > PARTICULAR_MAX_LENGTH) {
            throw new ShareTransferValidationException(
                    "Particulars/Remarks must be " + PARTICULAR_MAX_LENGTH
                            + " characters or fewer (T_TRANS_AUTH.PARTICULAR limit).");
        }

        LocalDate businessDate = businessDateService.currentBusinessDate();
        LocalDateTime now = LocalDateTime.now();

        String debitRef = form.getDebitReference().trim();
        String creditRef = form.getCreditReference().trim();
        BigDecimal quantity = form.getShareQuantity();
        String authTrCode = shortTrCode(form.getTransferType());

        String trId = workflowIdService.generateNextTransAuthTrId();

        TransAuth debitLeg = new TransAuth();
        debitLeg.setOid(workflowIdService.generateNextTransAuthOid());
        debitLeg.setFolioBo(debitRef);
        debitLeg.setTrId(trId);
        debitLeg.setTrCode(authTrCode);
        debitLeg.setTrDate(businessDate);
        debitLeg.setDrAmt(quantity);
        debitLeg.setCrAmt(BigDecimal.ZERO);
        debitLeg.setTrState(TransferAuthStatus.PENDING_CHECKER.getCode());
        debitLeg.setMakerId(makerId);
        debitLeg.setMakerIp(makerIp);
        debitLeg.setContraAccNo(creditRef);
        debitLeg.setInstrNo(form.getInstrumentNo());
        debitLeg.setInstrDate(form.getInstrumentDate());
        debitLeg.setParticular(particular);
        debitLeg.setModifyDate(now);

        transAuthRepository.saveAndFlush(debitLeg);

        TransAuth creditLeg = new TransAuth();
        creditLeg.setOid(workflowIdService.generateNextTransAuthOid());
        creditLeg.setFolioBo(creditRef);
        creditLeg.setTrId(trId);
        creditLeg.setTrCode(authTrCode);
        creditLeg.setTrDate(businessDate);
        creditLeg.setDrAmt(BigDecimal.ZERO);
        creditLeg.setCrAmt(quantity);
        creditLeg.setTrState(TransferAuthStatus.PENDING_CHECKER.getCode());
        creditLeg.setMakerId(makerId);
        creditLeg.setMakerIp(makerIp);
        creditLeg.setContraAccNo(debitRef);
        creditLeg.setInstrNo(form.getInstrumentNo());
        creditLeg.setInstrDate(form.getInstrumentDate());
        creditLeg.setParticular(particular);
        creditLeg.setModifyDate(now);

        transAuthRepository.save(creditLeg);

        return trId;
    }

    public List<ShareTransferRequestSummary> getMyRequests(
            String makerId,
            String statusFilter) {

        List<TransAuth> debitLegs;

        if (statusFilter == null
                || statusFilter.isBlank()
                || "ALL".equalsIgnoreCase(statusFilter)) {

            debitLegs = transAuthRepository.findMakerDebitLegs(makerId);

        } else {

            TransferAuthStatus status;

            try {
                status = TransferAuthStatus.valueOf(statusFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ShareTransferValidationException(
                        "Unknown status filter: " + statusFilter);
            }

            debitLegs = transAuthRepository.findMakerDebitLegsByState(
                    makerId, status.getCode());
        }

        return debitLegs.stream()
                .map(ShareTransferRequestSummary::fromDebitLeg)
                .collect(Collectors.toList());
    }

    public List<ShareTransferRequestSummary> getReturnedTransferRequests(String makerId) {

        List<TransAuth> debitLegs =
                transAuthRepository.findMakerDebitLegsByState(
                        makerId,
                        TransferAuthStatus.RETURNED_FOR_MODIFICATION.getCode()
                );

        return debitLegs.stream()
                .map(ShareTransferRequestSummary::fromDebitLeg)
                .collect(Collectors.toList());
    }

    public ReturnedTransferEditView getReturnedRequestForEdit(String trId, String makerId) {

        TransAuth debitLeg = findDebitLeg(trId);

        if (debitLeg.getTrState() == null
                || debitLeg.getTrState() != TransferAuthStatus.RETURNED_FOR_MODIFICATION.getCode()) {

            throw new ShareTransferValidationException(
                    "This transfer request is not returned for modification.");
        }

        if (makerId == null || !makerId.equals(debitLeg.getMakerId())) {

            throw new ShareTransferValidationException(
                    "You are not authorized to edit this request.");
        }

        ShareTransferForm form = new ShareTransferForm();

        form.setTransferType(fromShortCode(debitLeg.getTrCode()));
        form.setDebitReference(debitLeg.getFolioBo());
        form.setCreditReference(trimOrNull(debitLeg.getContraAccNo()));
        form.setShareQuantity(debitLeg.getDrAmt());
        form.setInstrumentNo(debitLeg.getInstrNo());
        form.setInstrumentDate(debitLeg.getInstrDate());
        form.setParticulars(debitLeg.getParticular());

        return new ReturnedTransferEditView(trId, form, debitLeg.getRemarks());
    }

    @Transactional
    public void resubmitReturned(
            String trId,
            ShareTransferForm form,
            String makerId,
            String makerIp) {

        TransAuth debitLeg = findDebitLeg(trId);
        TransAuth creditLeg = findCreditLeg(trId);

        if (debitLeg.getTrState() == null
                || debitLeg.getTrState() != TransferAuthStatus.RETURNED_FOR_MODIFICATION.getCode()) {

            throw new ShareTransferValidationException(
                    "This transfer request is not returned for modification.");
        }

        if (makerId == null || !makerId.equals(debitLeg.getMakerId())) {

            throw new ShareTransferValidationException(
                    "Only the original maker can resubmit this request.");
        }

        validationService.validateForSubmit(form);

        String particular = form.getParticulars();

        if (particular != null && particular.length() > PARTICULAR_MAX_LENGTH) {
            throw new ShareTransferValidationException(
                    "Particulars/Remarks must be " + PARTICULAR_MAX_LENGTH
                            + " characters or fewer (T_TRANS_AUTH.PARTICULAR limit).");
        }

        LocalDate businessDate = businessDateService.currentBusinessDate();
        LocalDateTime now = LocalDateTime.now();

        String debitRef = form.getDebitReference().trim();
        String creditRef = form.getCreditReference().trim();
        BigDecimal quantity = form.getShareQuantity();
        String authTrCode = shortTrCode(form.getTransferType());

        debitLeg.setFolioBo(debitRef);
        debitLeg.setContraAccNo(creditRef);
        debitLeg.setTrCode(authTrCode);
        debitLeg.setTrDate(businessDate);
        debitLeg.setDrAmt(quantity);
        debitLeg.setCrAmt(BigDecimal.ZERO);
        debitLeg.setTrState(TransferAuthStatus.RESUBMITTED.getCode());
        debitLeg.setMakerId(makerId);
        debitLeg.setMakerIp(makerIp);
        debitLeg.setCheckerId(null);
        debitLeg.setCheckerIp(null);
        debitLeg.setInstrNo(form.getInstrumentNo());
        debitLeg.setInstrDate(form.getInstrumentDate());
        debitLeg.setParticular(particular);
        debitLeg.setModifyDate(now);

        transAuthRepository.save(debitLeg);

        creditLeg.setFolioBo(creditRef);
        creditLeg.setContraAccNo(debitRef);
        creditLeg.setTrCode(authTrCode);
        creditLeg.setTrDate(businessDate);
        creditLeg.setDrAmt(BigDecimal.ZERO);
        creditLeg.setCrAmt(quantity);
        creditLeg.setTrState(TransferAuthStatus.RESUBMITTED.getCode());
        creditLeg.setMakerId(makerId);
        creditLeg.setMakerIp(makerIp);
        creditLeg.setCheckerId(null);
        creditLeg.setCheckerIp(null);
        creditLeg.setInstrNo(form.getInstrumentNo());
        creditLeg.setInstrDate(form.getInstrumentDate());
        creditLeg.setParticular(particular);
        creditLeg.setModifyDate(now);

        transAuthRepository.save(creditLeg);
    }

    private TransAuth findDebitLeg(String trId) {

        List<TransAuth> legs = transAuthRepository.findByTrId(trId);

        return legs.stream()
                .filter(t -> t.getDrAmt() != null && t.getDrAmt().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseThrow(() -> new ShareTransferValidationException(
                        "Transfer request not found: " + trId));
    }

    private TransAuth findCreditLeg(String trId) {

        List<TransAuth> legs = transAuthRepository.findByTrId(trId);

        return legs.stream()
                .filter(t -> t.getCrAmt() != null && t.getCrAmt().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElseThrow(() -> new ShareTransferValidationException(
                        "Transfer request not found: " + trId));
    }

    private String trimOrNull(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isAwaitingChecker(Integer trState) {
        return trState != null
                && (trState == TransferAuthStatus.PENDING_CHECKER.getCode()
                || trState == TransferAuthStatus.RESUBMITTED.getCode());
    }

    private String shortTrCode(TransferType type) {
        return switch (type) {
            case FOLIO_TO_FOLIO -> "F2F";
            case FOLIO_TO_BO -> "F2B";
            case BO_TO_FOLIO -> "B2F";
        };
    }

    private TransferType fromShortCode(String code) {

        if (code == null) {
            throw new ShareTransferValidationException(
                    "Missing transfer type code on stored request.");
        }

        return switch (code.trim().toUpperCase()) {
            case "F2F" -> TransferType.FOLIO_TO_FOLIO;
            case "F2B" -> TransferType.FOLIO_TO_BO;
            case "B2F" -> TransferType.BO_TO_FOLIO;
            default -> throw new ShareTransferValidationException(
                    "Unknown TR_CODE on T_TRANS_AUTH: " + code);
        };
    }


    public List<ShareTransferCheckerSummary> getPendingCheckerRequests() {

        return transAuthRepository.findPendingCheckerDebitLegs()
                .stream()
                .map(ShareTransferCheckerSummary::fromDebitLeg)
                .collect(Collectors.toList());
    }


    public ShareTransferCheckerDetail getCheckerDetail(String trId) {

        TransAuth debitLeg = findDebitLeg(trId);
        TransAuth creditLeg = findCreditLeg(trId);

        if (!isAwaitingChecker(debitLeg.getTrState())) {

            throw new ShareTransferValidationException(
                    "This transfer is no longer pending checker approval.");
        }

        return new ShareTransferCheckerDetail(
                debitLeg.getTrId(),
                debitLeg.getTrCode(),
                debitLeg.getTrDate(),
                debitLeg.getFolioBo(),
                trimOrNull(creditLeg.getFolioBo()),
                debitLeg.getDrAmt(),
                debitLeg.getInstrNo(),
                debitLeg.getInstrDate(),
                debitLeg.getParticular(),
                debitLeg.getRemarks(),
                debitLeg.getMakerId(),
                debitLeg.getMakerIp(),
                debitLeg.getModifyDate(),
                TransferAuthStatus.fromCode(debitLeg.getTrState())
        );
    }



    @Transactional
    public void returnForModification(
            String trId,
            String checkerId,
            String checkerIp,
            String remarks) {

        if (checkerId == null || checkerId.isBlank()) {
            throw new ShareTransferValidationException(
                    "Checker identity is required.");
        }

        if (remarks == null || remarks.trim().isEmpty()) {
            throw new ShareTransferValidationException(
                    "Remarks are required when returning a transfer for modification.");
        }

        String cleanRemarks = remarks.trim();

        if (cleanRemarks.length() > 255) {
            throw new ShareTransferValidationException(
                    "Remarks must be 255 characters or fewer.");
        }

        TransAuth debitLeg =
                transAuthRepository.findPendingDebitForUpdate(trId)
                        .orElseThrow(() ->
                                new ShareTransferValidationException(
                                        "Pending transfer not found: " + trId));

        TransAuth creditLeg =
                transAuthRepository.findCreditForUpdate(trId)
                        .orElseThrow(() ->
                                new ShareTransferValidationException(
                                        "Credit leg not found: " + trId));

        if (!isAwaitingChecker(debitLeg.getTrState())) {

            throw new ShareTransferValidationException(
                    "This transfer is no longer pending checker approval.");
        }

        if (debitLeg.getMakerId() != null
                && debitLeg.getMakerId().equals(checkerId)) {

            throw new ShareTransferValidationException(
                    "Maker cannot return their own transfer for modification.");
        }

        if (!trId.equals(creditLeg.getTrId())) {
            throw new ShareTransferValidationException(
                    "Transfer debit and credit legs do not match.");
        }

        debitLeg.setTrState(
                TransferAuthStatus.RETURNED_FOR_MODIFICATION.getCode());
        debitLeg.setCheckerId(checkerId);
        debitLeg.setCheckerIp(checkerIp);
        debitLeg.setRemarks(cleanRemarks);
        debitLeg.setModifyDate(LocalDateTime.now());

        creditLeg.setTrState(
                TransferAuthStatus.RETURNED_FOR_MODIFICATION.getCode());
        creditLeg.setCheckerId(checkerId);
        creditLeg.setCheckerIp(checkerIp);
        creditLeg.setRemarks(cleanRemarks);
        creditLeg.setModifyDate(LocalDateTime.now());

        transAuthRepository.save(debitLeg);
        transAuthRepository.save(creditLeg);
    }



    @Transactional
    public void rejectTransfer(
            String trId,
            String checkerId,
            String checkerIp,
            String remarks) {

        if (checkerId == null || checkerId.isBlank()) {
            throw new ShareTransferValidationException(
                    "Checker identity is required.");
        }

        if (remarks == null || remarks.trim().isEmpty()) {
            throw new ShareTransferValidationException(
                    "Remarks are required when rejecting a transfer.");
        }

        String cleanRemarks = remarks.trim();

        if (cleanRemarks.length() > 255) {
            throw new ShareTransferValidationException(
                    "Remarks must be 255 characters or fewer.");
        }

        TransAuth debitLeg =
                transAuthRepository.findPendingDebitForUpdate(trId)
                        .orElseThrow(() ->
                                new ShareTransferValidationException(
                                        "Pending transfer not found: " + trId));

        TransAuth creditLeg =
                transAuthRepository.findCreditForUpdate(trId)
                        .orElseThrow(() ->
                                new ShareTransferValidationException(
                                        "Credit leg not found: " + trId));

        if (!isAwaitingChecker(debitLeg.getTrState())) {

            throw new ShareTransferValidationException(
                    "This transfer is no longer pending checker approval.");
        }

        if (debitLeg.getMakerId() != null
                && debitLeg.getMakerId().equals(checkerId)) {

            throw new ShareTransferValidationException(
                    "Maker cannot reject their own transfer.");
        }

        if (!trId.equals(creditLeg.getTrId())) {
            throw new ShareTransferValidationException(
                    "Transfer debit and credit legs do not match.");
        }

        debitLeg.setTrState(
                TransferAuthStatus.REJECTED.getCode());
        debitLeg.setCheckerId(checkerId);
        debitLeg.setCheckerIp(checkerIp);
        debitLeg.setRemarks(cleanRemarks);
        debitLeg.setModifyDate(LocalDateTime.now());

        creditLeg.setTrState(
                TransferAuthStatus.REJECTED.getCode());
        creditLeg.setCheckerId(checkerId);
        creditLeg.setCheckerIp(checkerIp);
        creditLeg.setRemarks(cleanRemarks);
        creditLeg.setModifyDate(LocalDateTime.now());

        transAuthRepository.save(debitLeg);
        transAuthRepository.save(creditLeg);
    }



    @Transactional
    public void approveTransfer(
            String trId,
            String checkerId,
            String checkerIp) {

        if (checkerId == null || checkerId.isBlank()) {
            throw new ShareTransferValidationException(
                    "Checker identity is required.");
        }

        // ---------------------------------------------------------
        // 1. Lock the pending workflow debit leg
        // ---------------------------------------------------------

        TransAuth debitLeg =
                transAuthRepository.findPendingDebitForUpdate(trId)
                        .orElseThrow(() ->
                                new ShareTransferValidationException(
                                        "Pending transfer not found: " + trId));

        // ---------------------------------------------------------
        // 2. Lock the credit leg
        // ---------------------------------------------------------

        TransAuth creditLeg =
                transAuthRepository.findCreditForUpdate(trId)
                        .orElseThrow(() ->
                                new ShareTransferValidationException(
                                        "Credit leg not found: " + trId));

        // ---------------------------------------------------------
        // 3. Verify workflow state
        // ---------------------------------------------------------

        if (!isAwaitingChecker(debitLeg.getTrState())) {

            throw new ShareTransferValidationException(
                    "This transfer is no longer pending checker approval.");
        }

        // ---------------------------------------------------------
        // 4. Maker != Checker
        // ---------------------------------------------------------

        if (checkerId.equals(debitLeg.getMakerId())) {
            throw new ShareTransferValidationException(
                    "Maker cannot approve their own transfer.");
        }

        // ---------------------------------------------------------
        // 5. Verify the two legs belong together
        // ---------------------------------------------------------

        if (!trId.equals(creditLeg.getTrId())) {
            throw new ShareTransferValidationException(
                    "Transfer debit and credit legs do not match.");
        }

        // ---------------------------------------------------------
        // 6. Verify business date
        // ---------------------------------------------------------

        LocalDate businessDate = debitLeg.getTrDate();

        if (!businessDateService.isCurrentBusinessDate(businessDate)) {
            throw new ShareTransferValidationException(
                    "Transfer business date is not the current business date.");
        }

        // ---------------------------------------------------------
        // 7. Determine transfer type
        // ---------------------------------------------------------

        TransferType transferType =
                fromShortCode(debitLeg.getTrCode());

        BigDecimal quantity = debitLeg.getDrAmt();

        if (quantity == null
                || quantity.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ShareTransferValidationException(
                    "Transfer quantity must be greater than zero.");
        }

        String sourceRef = trimOrNull(debitLeg.getFolioBo());
        String destinationRef = trimOrNull(creditLeg.getFolioBo());

        if (sourceRef == null || destinationRef == null) {
            throw new ShareTransferValidationException(
                    "Transfer source and destination are required.");
        }


        // ---------------------------------------------------------
// FOLIO -> BO prototype path
// ---------------------------------------------------------

        if (transferType == TransferType.FOLIO_TO_BO) {

            List<Shareholder> sourceAccounts =
                    shareholderRepository.findAllByFolioBoInForUpdate(
                            List.of(sourceRef));

            if (sourceAccounts.size() != 1) {
                throw new ShareTransferValidationException(
                        "Source Folio account could not be found: "
                                + sourceRef);
            }

            Shareholder sourceAccount = sourceAccounts.get(0);

            validateAccountForApproval(sourceAccount, "Debit");

            long sourceBalance = accountBalance(sourceAccount);

            if (quantity.compareTo(
                    BigDecimal.valueOf(sourceBalance)) > 0) {

                throw new ShareTransferValidationException(
                        "Requested quantity (" + quantity
                                + ") exceeds available shares ("
                                + sourceBalance
                                + ") for Folio "
                                + sourceRef
                                + ".");
            }

            postFolioToBoPrototype(
                    trId,
                    debitLeg,
                    creditLeg,
                    checkerId,
                    checkerIp,
                    businessDate,
                    quantity,
                    sourceAccount
            );




            AccountCdbl boAccount = accountCdblRepository
                    .findByBoNo(creditLeg.getFolioBo())
                    .orElseThrow(() ->
                            new ShareTransferValidationException(
                                    "Destination BO account could not be found: "
                                            + creditLeg.getFolioBo()
                            ));

            BigDecimal currentBalance = boAccount.getCurrentBalance() == null
                    ? BigDecimal.ZERO
                    : boAccount.getCurrentBalance();

            BigDecimal freeBalance = boAccount.getFreeBalance() == null
                    ? BigDecimal.ZERO
                    : boAccount.getFreeBalance();

            boAccount.setCurrentBalance(
                    currentBalance.add(quantity)
            );

            boAccount.setFreeBalance(
                    freeBalance.add(quantity)
            );

            accountCdblRepository.saveAndFlush(boAccount);



            LocalDateTime now = LocalDateTime.now();

            debitLeg.setTrState(
                    TransferAuthStatus.APPROVED.getCode());
            debitLeg.setCheckerId(checkerId);
            debitLeg.setCheckerIp(checkerIp);
            debitLeg.setModifyDate(now);

            creditLeg.setTrState(
                    TransferAuthStatus.APPROVED.getCode());
            creditLeg.setCheckerId(checkerId);
            creditLeg.setCheckerIp(checkerIp);
            creditLeg.setModifyDate(now);

            transAuthRepository.save(debitLeg);
            transAuthRepository.save(creditLeg);

            BusinessAudit audit = new BusinessAudit();

            audit.setAuditId(
                    workflowIdService.nextBusinessAuditId());

            audit.setEventTime(now);
            audit.setModuleCode("SHARE_TRANSFER");
            audit.setActionType("APPROVE");
            audit.setEntityType("SHARE_TRANSFER");
            audit.setEntityId(trId);
            audit.setBusinessRef(trId);
            audit.setActorId(checkerId);
            audit.setClientIp(checkerIp);

            audit.setChangedFields(
                    "TR_STATE,BALANCE,"
                            + "T_SHARE_MOVEMENT,"
                            + "T_CDBL_OUT_BATCH,"
                            + "T_CDBL_OUT_ITEM");

            audit.setOldValue(
                    "{\"status\":\"PENDING_CHECKER\","
                            + "\"folioBalance\":"
                            + sourceBalance
                            + "}");

            audit.setNewValue(
                    "{\"status\":\"APPROVED\","
                            + "\"folioBalance\":"
                            + sourceAccount.getBalance()
                            + "}");

            audit.setCorrelationId("G-" + trId);
            audit.setRemarks(debitLeg.getRemarks());

            businessAuditRepository.save(audit);

            return;
        }

        // ---------------------------------------------------------
        // 8. Resolve effective ledger Folios
        // ---------------------------------------------------------

        String debitLedgerFolio;
        String creditLedgerFolio;

        switch (transferType) {

            case FOLIO_TO_FOLIO -> {
                debitLedgerFolio = sourceRef;
                creditLedgerFolio = destinationRef;
            }

            case FOLIO_TO_BO -> {
                debitLedgerFolio = sourceRef;
                creditLedgerFolio = TransferType.SETTLEMENT_FOLIO_BO;
            }

            case BO_TO_FOLIO -> {
                debitLedgerFolio = TransferType.SETTLEMENT_FOLIO_BO;
                creditLedgerFolio = destinationRef;
            }

            default -> throw new ShareTransferValidationException(
                    "Unsupported transfer type: " + transferType);
        }

        if (debitLedgerFolio.equals(creditLedgerFolio)) {
            throw new ShareTransferValidationException(
                    "Effective debit and credit Folios cannot be the same.");
        }

        // ---------------------------------------------------------
        // 9. Lock all physical accounts in deterministic order
        // ---------------------------------------------------------

        List<String> foliosToLock = new ArrayList<>(
                List.of(debitLedgerFolio, creditLedgerFolio));

        foliosToLock = foliosToLock.stream()
                .distinct()
                .sorted()
                .toList();

        List<Shareholder> lockedAccounts =
                shareholderRepository.findAllByFolioBoInForUpdate(
                        foliosToLock);

        if (lockedAccounts.size() != foliosToLock.size()) {
            throw new ShareTransferValidationException(
                    "One or more effective Folio accounts could not be found.");
        }

        Shareholder debitAccount = lockedAccounts.stream()
                .filter(s -> debitLedgerFolio.equals(s.getFolioBo()))
                .findFirst()
                .orElseThrow(() ->
                        new ShareTransferValidationException(
                                "Debit Folio account not found: "
                                        + debitLedgerFolio));

        Shareholder creditAccount = lockedAccounts.stream()
                .filter(s -> creditLedgerFolio.equals(s.getFolioBo()))
                .findFirst()
                .orElseThrow(() ->
                        new ShareTransferValidationException(
                                "Credit Folio account not found: "
                                        + creditLedgerFolio));

        // ---------------------------------------------------------
        // 10. Revalidate account status and lien
        // ---------------------------------------------------------

        validateAccountForApproval(debitAccount, "Debit");
        validateAccountForApproval(creditAccount, "Credit");

        // ---------------------------------------------------------
        // 11. Revalidate available physical shares
        // ---------------------------------------------------------

        long debitBalance = accountBalance(debitAccount);

        if (quantity.compareTo(BigDecimal.valueOf(debitBalance)) > 0) {
            throw new ShareTransferValidationException(
                    "Requested quantity (" + quantity
                            + ") exceeds available shares ("
                            + debitBalance
                            + ") for Folio "
                            + debitLedgerFolio
                            + ".");
        }




        // ---------------------------------------------------------
        // 12. Create ledger IDs
        // ---------------------------------------------------------

        String groupTrId = "G-" + trId;

        String debitTrId = trId + "-D";
        String creditTrId = trId + "-C";

        LocalDateTime now = LocalDateTime.now();

        // ---------------------------------------------------------
        // 13. Create debit ledger row
        // ---------------------------------------------------------

        TransShare debitLedger = new TransShare();

        debitLedger.setOid(
                workflowIdService.nextTransShareOid());

        debitLedger.setFolioBo(debitLedgerFolio);
        debitLedger.setTrId(debitTrId);
        debitLedger.setGrpTrId(groupTrId);
        debitLedger.setTrDate(businessDate);
        debitLedger.setTrType("201");
        debitLedger.setTrCode("04");
        debitLedger.setDrShare(quantity);
        debitLedger.setCrShare(BigDecimal.ZERO);
        debitLedger.setContraAcc(creditLedgerFolio);
        debitLedger.setInstrument(debitLeg.getInstrNo());
        debitLedger.setParticulars(
                debitLeg.getParticular() == null
                        ? "Share transfer " + trId
                        : debitLeg.getParticular());
        debitLedger.setUserId(checkerId);
        debitLedger.setIsValid(1);
        debitLedger.setPostDate(now);

        // ---------------------------------------------------------
        // 14. Create credit ledger row
        // ---------------------------------------------------------

        TransShare creditLedger = new TransShare();

        creditLedger.setOid(
                workflowIdService.nextTransShareOid());

        creditLedger.setFolioBo(creditLedgerFolio);
        creditLedger.setTrId(creditTrId);
        creditLedger.setGrpTrId(groupTrId);
        creditLedger.setTrDate(businessDate);
        creditLedger.setTrType("101");
        creditLedger.setTrCode("04");
        creditLedger.setDrShare(BigDecimal.ZERO);
        creditLedger.setCrShare(quantity);
        creditLedger.setContraAcc(debitLedgerFolio);
        creditLedger.setInstrument(creditLeg.getInstrNo());
        creditLedger.setParticulars(
                creditLeg.getParticular() == null
                        ? "Share transfer " + trId
                        : creditLeg.getParticular());
        creditLedger.setUserId(checkerId);
        creditLedger.setIsValid(1);
        creditLedger.setPostDate(now);

        // ---------------------------------------------------------
        // 15. Insert both ledger rows
        // ---------------------------------------------------------

        transShareRepository.save(debitLedger);
        transShareRepository.save(creditLedger);

        // ---------------------------------------------------------
        // 16. Update physical share balances
        // ---------------------------------------------------------

        long quantityLong;

        try {
            quantityLong = quantity.longValueExact();
        } catch (ArithmeticException e) {
            throw new ShareTransferValidationException(
                    "Share quantity must be a whole number.");
        }

        long newDebitBalance =
                Math.subtractExact(debitBalance, quantityLong);

        long newCreditBalance =
                Math.addExact(
                        accountBalance(creditAccount),
                        quantityLong);

        debitAccount.setBalance(newDebitBalance);
        creditAccount.setBalance(newCreditBalance);

// SHARES and BALANCE represent the same holding
// in this IBSBMS implementation.


        System.out.println("=== BEFORE SAVE ===");
        System.out.println("Debit Folio: " + debitAccount.getFolioBo());
        System.out.println("Debit Balance: " + debitAccount.getBalance());
        System.out.println("Credit Folio: " + creditAccount.getFolioBo());
        System.out.println("Credit Balance: " + creditAccount.getBalance());

        shareholderRepository.saveAndFlush(debitAccount);
        shareholderRepository.saveAndFlush(creditAccount);

        System.out.println("=== AFTER SAVE ===");
        System.out.println("Debit Balance: " + debitAccount.getBalance());
        System.out.println("Credit Balance: " + creditAccount.getBalance());
        // ---------------------------------------------------------
        // 17. Mark both workflow legs approved
        // ---------------------------------------------------------

        debitLeg.setTrState(
                TransferAuthStatus.APPROVED.getCode());
        debitLeg.setCheckerId(checkerId);
        debitLeg.setCheckerIp(checkerIp);
        debitLeg.setModifyDate(now);

        creditLeg.setTrState(
                TransferAuthStatus.APPROVED.getCode());
        creditLeg.setCheckerId(checkerId);
        creditLeg.setCheckerIp(checkerIp);
        creditLeg.setModifyDate(now);

        transAuthRepository.save(debitLeg);
        transAuthRepository.save(creditLeg);

        // ---------------------------------------------------------
        // 18. Business audit
        // ---------------------------------------------------------

        BusinessAudit audit = new BusinessAudit();

        audit.setAuditId(
                workflowIdService.nextBusinessAuditId());

        audit.setEventTime(now);
        audit.setModuleCode("SHARE_TRANSFER");
        audit.setActionType("APPROVE");
        audit.setEntityType("SHARE_TRANSFER");
        audit.setEntityId(trId);
        audit.setBusinessRef(trId);
        audit.setActorId(checkerId);
        audit.setClientIp(checkerIp);
        audit.setChangedFields(
                "TR_STATE,SHARES,T_TRANS_SHARE");
        audit.setOldValue(
                "{\"status\":\"PENDING_CHECKER\"}");
        audit.setNewValue(
                "{\"status\":\"APPROVED\","
                        + "\"groupTrId\":\"" + groupTrId + "\"}");
        audit.setCorrelationId(groupTrId);
        audit.setRemarks(debitLeg.getRemarks());

        businessAuditRepository.save(audit);
    }


    private void validateAccountForApproval(
            Shareholder account,
            String side) {

        if (account.getIsValid() == null
                || account.getIsValid() != 1) {

            throw new ShareTransferValidationException(
                    side + " Folio " + account.getFolioBo()
                            + " is not active.");
        }

        if (account.getIsLien() != null
                && account.getIsLien() == 1) {

            throw new ShareTransferValidationException(
                    side + " Folio " + account.getFolioBo()
                            + " is under lien; transfer is not allowed.");
        }
    }



    private long accountBalance(Shareholder account) {
        return account.getBalance() == null
                ? 0L
                : account.getBalance();
    }


    private void postFolioToBoPrototype(
            String trId,
            TransAuth debitLeg,
            TransAuth creditLeg,
            String checkerId,
            String checkerIp,
            LocalDate businessDate,
            BigDecimal quantity,
            Shareholder sourceAccount) {

        long quantityLong;

        try {
            quantityLong = quantity.longValueExact();
        } catch (ArithmeticException e) {
            throw new ShareTransferValidationException(
                    "Share quantity must be a whole number.");
        }

        long sourceBalance = accountBalance(sourceAccount);

        if (quantityLong <= 0) {
            throw new ShareTransferValidationException(
                    "Transfer quantity must be greater than zero.");
        }

        if (quantityLong > sourceBalance) {
            throw new ShareTransferValidationException(
                    "Requested quantity (" + quantityLong
                            + ") exceeds available shares ("
                            + sourceBalance
                            + ") for Folio "
                            + sourceAccount.getFolioBo()
                            + ".");
        }

        LocalDateTime now = LocalDateTime.now();

        /*
         * 1. Create movement
         */
        Long movementId = workflowIdService.nextShareMovementId();

        String movementRef =
                "MV-" + businessDate.toString().replace("-", "")
                        + "-" + String.format("%06d", movementId);

        String groupTrId = "G-" + trId;

        ShareMovement movement = new ShareMovement();

        movement.setMovementId(movementId);
        movement.setMovementRef(movementRef);
        movement.setMovementType("DEMAT");
        movement.setSourceType("FOLIO");
        movement.setSourceRef(sourceAccount.getFolioBo());
        movement.setTargetType("BO");
        movement.setTargetRef(creditLeg.getFolioBo());
        movement.setQuantity(quantity);
        movement.setParticulars(
                debitLeg.getParticular() == null
                        ? "Folio to BO transfer " + trId
                        : debitLeg.getParticular());
        movement.setInstrument(debitLeg.getInstrNo());
        movement.setOriginalTrId(trId);
        movement.setGroupTrId(groupTrId);
        movement.setLocalStatus("POSTED");
        movement.setCdblStatus("NOT_SENT");
        movement.setRequestedBy(debitLeg.getMakerId());
        movement.setRequestedIp(debitLeg.getMakerIp());
        movement.setRequestedAt(debitLeg.getModifyDate());
        movement.setPostedBy(checkerId);
        movement.setPostedAt(now);
        movement.setBusinessDate(businessDate);


        System.out.println("=== F2B SHARE MOVEMENT DEBUG ===");
        System.out.println("LOCAL_STATUS = [" + movement.getLocalStatus() + "]");
        System.out.println("CDBL_STATUS  = [" + movement.getCdblStatus() + "]");
        System.out.println("MOVEMENT_TYPE = [" + movement.getMovementType() + "]");
        System.out.println("SOURCE_TYPE = [" + movement.getSourceType() + "]");
        System.out.println("TARGET_TYPE = [" + movement.getTargetType() + "]");
        System.out.println("===============================");


        shareMovementRepository.save(movement);

        /*
         * 2. Create CDBL outgoing batch
         */
        Long batchId = workflowIdService.nextCdblOutBatchId();

        CdblOutBatch batch = new CdblOutBatch();

        batch.setOutBatchId(batchId);
        batch.setBatchRef("OUT-" + movementRef);
        batch.setStatus("APPROVED");
        batch.setApprovalRequestId(null);
        batch.setMovementCount(1L);
        batch.setTotalQuantity(quantityLong);
        batch.setCreatedBy(checkerId);
        batch.setCreatedIp(checkerIp);
        batch.setCreatedAt(now);
        batch.setVersionNo(1);

        cdblOutBatchRepository.save(batch);

        /*
         * 3. Create CDBL outgoing item
         */
        Long itemId = workflowIdService.nextCdblOutItemId();

        CdblOutItem item = new CdblOutItem();

        item.setOutItemId(itemId);
        item.setOutBatchId(batchId);
        item.setMovementId(movementId);
        item.setMovementRef(movementRef);
        item.setMovementType("DEMAT");
        item.setFolioNo(sourceAccount.getFolioBo());
        item.setBoNo(creditLeg.getFolioBo());
        item.setQuantity(quantityLong);
        item.setCreatedAt(now);

        cdblOutItemRepository.save(item);

        /*
         * 4. Deduct source Folio balance
         */
        long newBalance;

        try {
            newBalance = Math.subtractExact(
                    sourceBalance,
                    quantityLong);
        } catch (ArithmeticException e) {
            throw new ShareTransferValidationException(
                    "Folio balance calculation overflowed.");
        }

        sourceAccount.setBalance(newBalance);

        shareholderRepository.saveAndFlush(sourceAccount);
    }
}