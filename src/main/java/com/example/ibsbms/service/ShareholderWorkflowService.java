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

        /*
         * Never trust Folio BO supplied by the browser.
         */
        request.getBasicInfo().setFolioBo(null);

        /*
         * Generate the next physical Folio.
         */
        String folioBo = workflowIdService.generateNextFolioBo();

        /*
         * Make sure this Folio isn't already reserved
         * by another shareholder Create request.
         */
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

        /*
         * Put the server-generated Folio into the proposal.
         */
        request.getBasicInfo().setFolioBo(folioBo);

        /*
         * Build the complete NEW_VALUE JSON.
         */
        String proposalJson =
                shareholderService.buildCreateProposalJson(request);

        /*
         * Generate the Change ID.
         */
        String changeId =
                workflowIdService.generateChangeId();

        /*
         * Create T_SHAREHOLDER_CHANGE_REQUEST.
         */
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

        /*
         * Generate the Approval Request ID.
         */
        Long requestId =
                workflowIdService.nextApprovalRequestId();

        /*
         * Current Bangladesh business date.
         *
         * The server determines this.
         * It does not come from the browser.
         */
        LocalDate businessDate =
                LocalDate.now(ZoneId.of("Asia/Dhaka"));

        /*
         * Create T_APPROVAL_REQUEST.
         */
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


        /*
         * Record the maker's submission in T_APPROVAL_ACTION.
         */
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
}