package com.example.ibsbms.service;

import com.example.ibsbms.dto.ShareTransferForm;
import com.example.ibsbms.dto.ShareTransferRequestSummary;
import com.example.ibsbms.entity.TransAuth;
import com.example.ibsbms.enums.TransferAuthStatus;
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
        String authTrCode = form.getTransferType().getTrCode();

        String trId = workflowIdService.generateNextTransAuthTrId();

        TransAuth debitLeg = new TransAuth();
        debitLeg.setOid(workflowIdService.generateNextTransAuthOid());
        debitLeg.setFolioBo(debitRef);
        debitLeg.setTrId(trId);
        debitLeg.setTrCode(authTrCode);
        debitLeg.setTrDate(businessDate);
        debitLeg.setDrAmt(quantity);
        debitLeg.setCrAmt(BigDecimal.ZERO);
        debitLeg.setTrState(0);
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
        creditLeg.setTrState(0);
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

    /**
     * Task 17 - "My Pending / Returned" list for Maker.
     * <p>
     * Returns one summary row per transfer request submitted by this
     * maker (debit leg only - see ShareTransferRequestSummary), newest
     * first, optionally filtered by TransferAuthStatus.
     * <p>
     * Returns the full (unpaginated) list, same as
     * ApprovalWorkflowService.searchMakerRequests() - the controller
     * paginates in memory. See the note on TransAuthRepository for why
     * DB-level pagination (Pageable) is avoided here.
     *
     * @param makerId      the authenticated maker's user id
     * @param statusFilter null/blank/"ALL" for everything, otherwise a
     *                     TransferAuthStatus name such as
     *                     "PENDING_CHECKER" or "RETURNED_FOR_MODIFICATION"
     */
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

}