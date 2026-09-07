package com.example.ibsbms.service;

import com.example.ibsbms.entity.ApprovalAction;
import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.repository.ApprovalActionRepository;
import com.example.ibsbms.repository.ApprovalRequestRepository;
import com.example.ibsbms.repository.ShareholderChangeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ApprovalWorkflowService {

    private static final String PENDING_CHECKER = "PENDING_CHECKER";
    private static final String CHECKER = "CHECKER";

    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String RETURNED_FOR_MODIFICATION =
            "RETURNED_FOR_MODIFICATION";

    private static final String COMPLETED = "COMPLETED";
    private static final String MAKER = "MAKER";

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalActionRepository approvalActionRepository;
    private final ShareholderChangeRequestRepository changeRequestRepository;
    private final WorkflowIdService workflowIdService;

    public ApprovalWorkflowService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalActionRepository approvalActionRepository,
            ShareholderChangeRequestRepository changeRequestRepository,
            WorkflowIdService workflowIdService) {

        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalActionRepository = approvalActionRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.workflowIdService = workflowIdService;
    }

    public List<ApprovalRequest> getPendingCheckerRequests() {

        return approvalRequestRepository
                .findPendingCheckerRequests();
    }

    public ApprovalRequest getApprovalRequest(Long requestId) {

        return approvalRequestRepository
                .findByRequestId(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Approval request not found: " + requestId
                        ));
    }

    public ShareholderChangeRequest getChangeRequest(
            ApprovalRequest approvalRequest) {

        return changeRequestRepository
                .findByChangeId(approvalRequest.getSourceId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Shareholder change request not found: "
                                        + approvalRequest.getSourceId()
                        ));
    }

    public List<ApprovalAction> getApprovalHistory(
            Long requestId) {

        return approvalActionRepository
                .findByRequestIdOrderByActionAtAsc(requestId);
    }

    @Transactional
    public void approve(
            Long requestId,
            String checkerId,
            String checkerIp,
            String remarks) {

        ApprovalRequest approvalRequest =
                getApprovalRequest(requestId);

        validateCheckerAction(
                approvalRequest,
                checkerId
        );

        /*
         * Maker and Checker must be different.
         */
        if (approvalRequest.getMakerId().equals(checkerId)) {

            throw new IllegalStateException(
                    "Maker cannot approve their own request."
            );
        }

        /*
         * Verify that this request belongs to the
         * current Bangladesh business date.
         */
        validateBusinessDate(approvalRequest);

        /*
         * Update approval request.
         */
        approvalRequest.setStatus(APPROVED);
        approvalRequest.setCurrentStage(COMPLETED);
        approvalRequest.setCheckerId(checkerId);
        approvalRequest.setCheckerIp(checkerIp);
        approvalRequest.setUpdatedAt(LocalDateTime.now());
        approvalRequest.setDecidedAt(LocalDateTime.now());

        Integer version = approvalRequest.getVersionNo();

        if (version == null) {
            version = 0;
        }

        approvalRequest.setVersionNo(version + 1);

        approvalRequestRepository.save(approvalRequest);

        /*
         * Record checker approval action.
         */
        saveAction(
                requestId,
                CHECKER,
                "APPROVED",
                checkerId,
                checkerIp,
                remarks
        );

        /*
         * IMPORTANT:
         *
         * We are deliberately NOT inserting into
         * T_ACCOUNT_SHARE yet.
         *
         * That is the next step after we verify that
         * the database-backed approval state works.
         */
    }

    @Transactional
    public void returnForModification(
            Long requestId,
            String checkerId,
            String checkerIp,
            String remarks) {

        if (remarks == null || remarks.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Remarks are required when returning a request."
            );
        }

        ApprovalRequest approvalRequest =
                getApprovalRequest(requestId);

        validateCheckerAction(
                approvalRequest,
                checkerId
        );

        validateBusinessDate(approvalRequest);

        approvalRequest.setStatus(
                RETURNED_FOR_MODIFICATION
        );

        approvalRequest.setCurrentStage(MAKER);
        approvalRequest.setCheckerId(checkerId);
        approvalRequest.setCheckerIp(checkerIp);
        approvalRequest.setUpdatedAt(LocalDateTime.now());

        Integer version = approvalRequest.getVersionNo();

        if (version == null) {
            version = 0;
        }

        approvalRequest.setVersionNo(version + 1);

        approvalRequestRepository.save(approvalRequest);

        saveAction(
                requestId,
                CHECKER,
                "RETURNED_FOR_MODIFICATION",
                checkerId,
                checkerIp,
                remarks
        );
    }

    @Transactional
    public void reject(
            Long requestId,
            String checkerId,
            String checkerIp,
            String remarks) {

        if (remarks == null || remarks.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Remarks are required when rejecting a request."
            );
        }

        ApprovalRequest approvalRequest =
                getApprovalRequest(requestId);

        validateCheckerAction(
                approvalRequest,
                checkerId
        );

        validateBusinessDate(approvalRequest);

        approvalRequest.setStatus(REJECTED);
        approvalRequest.setCurrentStage(COMPLETED);
        approvalRequest.setCheckerId(checkerId);
        approvalRequest.setCheckerIp(checkerIp);
        approvalRequest.setUpdatedAt(LocalDateTime.now());
        approvalRequest.setDecidedAt(LocalDateTime.now());

        Integer version = approvalRequest.getVersionNo();

        if (version == null) {
            version = 0;
        }

        approvalRequest.setVersionNo(version + 1);

        approvalRequestRepository.save(approvalRequest);

        saveAction(
                requestId,
                CHECKER,
                "REJECTED",
                checkerId,
                checkerIp,
                remarks
        );
    }

    private void validateCheckerAction(
            ApprovalRequest approvalRequest,
            String checkerId) {

        if (!PENDING_CHECKER.equals(
                approvalRequest.getStatus())) {

            throw new IllegalStateException(
                    "Request is no longer pending."
            );
        }

        if (!CHECKER.equals(
                approvalRequest.getCurrentStage())) {

            throw new IllegalStateException(
                    "Request is not currently at Checker stage."
            );
        }

        if (checkerId == null ||
                checkerId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Checker ID is required."
            );
        }
    }

    private void validateBusinessDate(
            ApprovalRequest approvalRequest) {

        LocalDate currentBusinessDate =
                LocalDate.now(
                        ZoneId.of("Asia/Dhaka")
                );

        if (!currentBusinessDate.equals(
                approvalRequest.getBusinessDate())) {

            throw new IllegalStateException(
                    "This approval request is not from the "
                            + "current business date."
            );
        }
    }

    private void saveAction(
            Long requestId,
            String stage,
            String action,
            String actorId,
            String actorIp,
            String remarks) {

        Long actionId =
                workflowIdService.nextApprovalActionId();

        ApprovalAction actionEntity =
                new ApprovalAction();

        actionEntity.setActionId(actionId);
        actionEntity.setRequestId(requestId);
        actionEntity.setStage(stage);
        actionEntity.setAction(action);
        actionEntity.setActorId(actorId);
        actionEntity.setActorIp(actorIp);
        actionEntity.setRemarks(remarks);
        actionEntity.setActionAt(LocalDateTime.now());

        approvalActionRepository.save(actionEntity);
    }
}