package com.example.ibsbms.service;

import com.example.ibsbms.dto.ReturnedTransferEditView;
import com.example.ibsbms.dto.ShareTransferForm;
import com.example.ibsbms.dto.ShareTransferRequestSummary;
import com.example.ibsbms.entity.TransAuth;
import com.example.ibsbms.enums.TransferAuthStatus;
import com.example.ibsbms.enums.TransferType;
import com.example.ibsbms.exception.ShareTransferValidationException;
import com.example.ibsbms.repository.TransAuthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShareTransferWorkflowService {

    private static final int PARTICULAR_MAX_LENGTH = 255;

    private final ShareTransferValidationService validationService;
    private final TransAuthRepository transAuthRepository;
    private final WorkflowIdService workflowIdService;
    private final BusinessDateService businessDateService;

    public ShareTransferWorkflowService(
            ShareTransferValidationService validationService,
            TransAuthRepository transAuthRepository,
            WorkflowIdService workflowIdService,
            BusinessDateService businessDateService) {

        this.validationService = validationService;
        this.transAuthRepository = transAuthRepository;
        this.workflowIdService = workflowIdService;
        this.businessDateService = businessDateService;
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
        debitLeg.setTrState(TransferAuthStatus.PENDING_CHECKER.getCode());
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
        creditLeg.setTrState(TransferAuthStatus.PENDING_CHECKER.getCode());
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
}