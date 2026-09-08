package com.example.ibsbms.service;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.repository.ApprovalRequestRepository;
import com.example.ibsbms.repository.ShareholderChangeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ibsbms.entity.ApprovalAction;
import com.example.ibsbms.repository.ApprovalActionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ShareholderWorkflowService {

    private final ShareholderService shareholderService;
    private final WorkflowIdService workflowIdService;
    private final ShareholderChangeRequestRepository changeRequestRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalActionRepository approvalActionRepository;

    public ShareholderWorkflowService(
            ShareholderService shareholderService,
            WorkflowIdService workflowIdService,
            ShareholderChangeRequestRepository changeRequestRepository,
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalActionRepository approvalActionRepository) {

        this.shareholderService = shareholderService;
        this.workflowIdService = workflowIdService;
        this.changeRequestRepository = changeRequestRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalActionRepository = approvalActionRepository;
    }

    @Transactional
    public void submitCreateForApproval(
            ShareholderCreateRequest request,
            String makerId,
            String makerIp) {

        request.getBasicInfo().setFolioBo(null);

        String folioBo = workflowIdService.generateNextFolioBo();

        boolean alreadyReserved =
                changeRequestRepository.existsByFolioBoAndOperationCode(
                        folioBo,
                        "SHAREHOLDER_CREATE"
                );

        if (alreadyReserved) {
            throw new IllegalStateException(
                    "Generated Folio BO is already reserved: " + folioBo
            );
        }

        request.getBasicInfo().setFolioBo(folioBo);

        String proposalJson =
                shareholderService.buildCreateProposalJson(request);

        String changeId =
                workflowIdService.generateChangeId();

        ShareholderChangeRequest changeRequest =
                new ShareholderChangeRequest();

        changeRequest.setChangeId(changeId);
        changeRequest.setFolioBo(folioBo);
        changeRequest.setOperationCode("SHAREHOLDER_CREATE");
        changeRequest.setOldValue(null);
        changeRequest.setNewValue(proposalJson);
        changeRequest.setCreatedBy(makerId);
        changeRequest.setCreatedIp(makerIp);
        changeRequest.setCreatedAt(LocalDateTime.now());
        changeRequest.setUpdatedAt(LocalDateTime.now());
        changeRequest.setVersionNo(0);

        changeRequestRepository.save(changeRequest);

        Long requestId =
                workflowIdService.nextApprovalRequestId();

        LocalDate businessDate =
                LocalDate.now(ZoneId.of("Asia/Dhaka"));

        ApprovalRequest approvalRequest =
                new ApprovalRequest();

        approvalRequest.setRequestId(requestId);
        approvalRequest.setOperationCode("SHAREHOLDER_CREATE");
        approvalRequest.setEntityType("SHAREHOLDER");
        approvalRequest.setEntityId(folioBo);
        approvalRequest.setSourceType("SHAREHOLDER_CHANGE");
        approvalRequest.setSourceId(changeId);
        approvalRequest.setBusinessRef(folioBo);
        approvalRequest.setStatus("PENDING_CHECKER");
        approvalRequest.setCurrentStage("CHECKER");
        approvalRequest.setMakerId(makerId);
        approvalRequest.setMakerIp(makerIp);
        approvalRequest.setCheckerId(null);
        approvalRequest.setCheckerIp(null);
        approvalRequest.setApproverId(null);
        approvalRequest.setApproverIp(null);
        approvalRequest.setCreatedAt(LocalDateTime.now());
        approvalRequest.setUpdatedAt(LocalDateTime.now());
        approvalRequest.setDecidedAt(null);
        approvalRequest.setVersionNo(0);
        approvalRequest.setBusinessDate(businessDate);

        approvalRequestRepository.save(approvalRequest);

        Long actionId =
                workflowIdService.nextApprovalActionId();

        ApprovalAction approvalAction =
                new ApprovalAction();

        approvalAction.setActionId(actionId);
        approvalAction.setRequestId(requestId);
        approvalAction.setStage("MAKER");
        approvalAction.setAction("SUBMITTED");
        approvalAction.setActorId(makerId);
        approvalAction.setActorIp(makerIp);
        approvalAction.setRemarks(null);
        approvalAction.setActionAt(LocalDateTime.now());

        approvalActionRepository.save(approvalAction);

        System.out.println("======================================");
        System.out.println("APPROVAL REQUEST SAVED");
        System.out.println("======================================");
        System.out.println("Change ID      : " + changeId);
        System.out.println("Folio BO       : " + folioBo);
        System.out.println("Request ID     : " + requestId);
        System.out.println("Maker ID       : " + makerId);
        System.out.println("Operation      : SHAREHOLDER_CREATE");
        System.out.println("Status         : PENDING_CHECKER");
        System.out.println("Current Stage  : CHECKER");
        System.out.println("Business Date  : " + businessDate);
        System.out.println("======================================");
    }

    @Transactional
    public void submitModifyForApproval(
            String folioBo,
            ShareholderCreateRequest edited,
            String makerId,
            String makerIp) {

        edited.getBasicInfo().setFolioBo(folioBo);

        boolean hasActiveRequest = !approvalRequestRepository
                .findByBusinessRefAndStatusIn(
                        folioBo,
                        List.of("PENDING_CHECKER", "RETURNED_FOR_MODIFICATION")
                )
                .isEmpty();

        if (hasActiveRequest) {
            throw new IllegalStateException(
                    "There is already a pending change request for Folio/BO " + folioBo);
        }

        ShareholderCreateRequest currentSnapshot =
                shareholderService.snapshotOf(folioBo);

        String oldValueJson =
                shareholderService.buildCreateProposalJson(currentSnapshot);

        String newValueJson =
                shareholderService.buildCreateProposalJson(edited);

        String changeId =
                workflowIdService.generateChangeId();

        ShareholderChangeRequest changeRequest =
                new ShareholderChangeRequest();

        changeRequest.setChangeId(changeId);
        changeRequest.setFolioBo(folioBo);
        changeRequest.setOperationCode("SHAREHOLDER_UPDATE");
        changeRequest.setOldValue(oldValueJson);
        changeRequest.setNewValue(newValueJson);
        changeRequest.setCreatedBy(makerId);
        changeRequest.setCreatedIp(makerIp);
        changeRequest.setCreatedAt(LocalDateTime.now());
        changeRequest.setUpdatedAt(LocalDateTime.now());
        changeRequest.setVersionNo(0);

        changeRequestRepository.save(changeRequest);

        Long requestId =
                workflowIdService.nextApprovalRequestId();

        LocalDate businessDate =
                LocalDate.now(ZoneId.of("Asia/Dhaka"));

        ApprovalRequest approvalRequest =
                new ApprovalRequest();

        approvalRequest.setRequestId(requestId);
        approvalRequest.setOperationCode("SHAREHOLDER_UPDATE");
        approvalRequest.setEntityType("SHAREHOLDER");
        approvalRequest.setEntityId(folioBo);
        approvalRequest.setSourceType("SHAREHOLDER_CHANGE");
        approvalRequest.setSourceId(changeId);
        approvalRequest.setBusinessRef(folioBo);
        approvalRequest.setStatus("PENDING_CHECKER");
        approvalRequest.setCurrentStage("CHECKER");
        approvalRequest.setMakerId(makerId);
        approvalRequest.setMakerIp(makerIp);
        approvalRequest.setCheckerId(null);
        approvalRequest.setCheckerIp(null);
        approvalRequest.setApproverId(null);
        approvalRequest.setApproverIp(null);
        approvalRequest.setCreatedAt(LocalDateTime.now());
        approvalRequest.setUpdatedAt(LocalDateTime.now());
        approvalRequest.setDecidedAt(null);
        approvalRequest.setVersionNo(0);
        approvalRequest.setBusinessDate(businessDate);

        approvalRequestRepository.save(approvalRequest);

        Long actionId =
                workflowIdService.nextApprovalActionId();

        ApprovalAction approvalAction =
                new ApprovalAction();

        approvalAction.setActionId(actionId);
        approvalAction.setRequestId(requestId);
        approvalAction.setStage("MAKER");
        approvalAction.setAction("SUBMITTED");
        approvalAction.setActorId(makerId);
        approvalAction.setActorIp(makerIp);
        approvalAction.setRemarks(null);
        approvalAction.setActionAt(LocalDateTime.now());

        approvalActionRepository.save(approvalAction);

        System.out.println("======================================");
        System.out.println("MODIFY REQUEST SAVED");
        System.out.println("======================================");
        System.out.println("Change ID      : " + changeId);
        System.out.println("Folio BO       : " + folioBo);
        System.out.println("Request ID     : " + requestId);
        System.out.println("Maker ID       : " + makerId);
        System.out.println("Operation      : SHAREHOLDER_UPDATE");
        System.out.println("Status         : PENDING_CHECKER");
        System.out.println("Current Stage  : CHECKER");
        System.out.println("Business Date  : " + businessDate);
        System.out.println("======================================");
    }

    @Transactional
    public void approve(
            Long requestId,
            String checkerId,
            String checkerIp,
            String remarks) {

        ApprovalRequest approvalRequest = approvalRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval request not found: " + requestId));

        if (!"PENDING_CHECKER".equals(approvalRequest.getStatus())) {
            throw new IllegalStateException("Request is no longer pending.");
        }

        if (approvalRequest.getMakerId().equals(checkerId)) {
            throw new IllegalStateException("Maker cannot approve their own request.");
        }

        ShareholderChangeRequest changeRequest = changeRequestRepository
                .findById(approvalRequest.getSourceId())
                .orElseThrow(() -> new IllegalStateException(
                        "Change request not found for approval: "
                                + approvalRequest.getSourceId()));

        ShareholderCreateRequest approvedData =
                shareholderService.parseProposalJson(changeRequest.getNewValue());

        shareholderService.applyApproved(
                approvalRequest.getBusinessRef(),
                approvedData,
                approvalRequest.getMakerId(),
                checkerId
        );

        LocalDateTime now = LocalDateTime.now();

        approvalRequest.setStatus("APPROVED");
        approvalRequest.setCurrentStage("COMPLETED");
        approvalRequest.setCheckerId(checkerId);
        approvalRequest.setCheckerIp(checkerIp);
        approvalRequest.setDecidedAt(now);
        approvalRequest.setUpdatedAt(now);

        approvalRequestRepository.save(approvalRequest);

        Long actionId = workflowIdService.nextApprovalActionId();

        ApprovalAction approvalAction = new ApprovalAction();
        approvalAction.setActionId(actionId);
        approvalAction.setRequestId(requestId);
        approvalAction.setStage("CHECKER");
        approvalAction.setAction("APPROVED");
        approvalAction.setActorId(checkerId);
        approvalAction.setActorIp(checkerIp);
        approvalAction.setRemarks(remarks);
        approvalAction.setActionAt(now);

        approvalActionRepository.save(approvalAction);
    }

    @Transactional
    public void returnForModification(
            Long requestId,
            String checkerId,
            String checkerIp,
            String remarks) {

        if (remarks == null || remarks.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Remarks are required when returning a request.");
        }

        ApprovalRequest approvalRequest = approvalRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval request not found: " + requestId));

        if (!"PENDING_CHECKER".equals(approvalRequest.getStatus())) {
            throw new IllegalStateException("Request is no longer pending.");
        }

        if (approvalRequest.getMakerId().equals(checkerId)) {
            throw new IllegalStateException("Maker cannot action their own request.");
        }

        LocalDateTime now = LocalDateTime.now();

        approvalRequest.setStatus("RETURNED_FOR_MODIFICATION");
        approvalRequest.setCurrentStage("MAKER");
        approvalRequest.setUpdatedAt(now);

        approvalRequestRepository.save(approvalRequest);

        Long actionId = workflowIdService.nextApprovalActionId();

        ApprovalAction approvalAction = new ApprovalAction();
        approvalAction.setActionId(actionId);
        approvalAction.setRequestId(requestId);
        approvalAction.setStage("CHECKER");
        approvalAction.setAction("RETURNED_FOR_MODIFICATION");
        approvalAction.setActorId(checkerId);
        approvalAction.setActorIp(checkerIp);
        approvalAction.setRemarks(remarks);
        approvalAction.setActionAt(now);

        approvalActionRepository.save(approvalAction);
    }

    @Transactional
    public void reject(
            Long requestId,
            String checkerId,
            String checkerIp,
            String remarks) {

        if (remarks == null || remarks.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Remarks are required when rejecting a request.");
        }

        ApprovalRequest approvalRequest = approvalRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval request not found: " + requestId));

        if (!"PENDING_CHECKER".equals(approvalRequest.getStatus())) {
            throw new IllegalStateException("Request is no longer pending.");
        }

        if (approvalRequest.getMakerId().equals(checkerId)) {
            throw new IllegalStateException("Maker cannot reject their own request.");
        }

        LocalDateTime now = LocalDateTime.now();

        approvalRequest.setStatus("REJECTED");
        approvalRequest.setCurrentStage("COMPLETED");
        approvalRequest.setCheckerId(checkerId);
        approvalRequest.setCheckerIp(checkerIp);
        approvalRequest.setDecidedAt(now);
        approvalRequest.setUpdatedAt(now);

        approvalRequestRepository.save(approvalRequest);

        Long actionId = workflowIdService.nextApprovalActionId();

        ApprovalAction approvalAction = new ApprovalAction();
        approvalAction.setActionId(actionId);
        approvalAction.setRequestId(requestId);
        approvalAction.setStage("CHECKER");
        approvalAction.setAction("REJECTED");
        approvalAction.setActorId(checkerId);
        approvalAction.setActorIp(checkerIp);
        approvalAction.setRemarks(remarks);
        approvalAction.setActionAt(now);

        approvalActionRepository.save(approvalAction);
    }
}