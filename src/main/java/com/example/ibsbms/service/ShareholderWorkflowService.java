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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

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

    private final ObjectMapper objectMapper;

    public ShareholderWorkflowService(
            ShareholderService shareholderService,
            WorkflowIdService workflowIdService,
            ShareholderChangeRequestRepository changeRequestRepository,
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalActionRepository approvalActionRepository,
            ObjectMapper objectMapper) {

        this.shareholderService = shareholderService;
        this.workflowIdService = workflowIdService;
        this.changeRequestRepository = changeRequestRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalActionRepository = approvalActionRepository;
        this.objectMapper = objectMapper;
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


    @Transactional
    public void submitModifyForApproval(
            String folioBo,
            ShareholderCreateRequest edited,
            String makerId,
            String makerIp) {

        /*
         * Never trust the Folio/BO on a Modify from the browser - it is
         * always the one already approved and currently being edited.
         */
        edited.getBasicInfo().setFolioBo(folioBo);

        /*
         * Block a second Modify (or an overlapping Create) from being
         * opened on a Folio/BO that already has a change request sitting
         * in PENDING_CHECKER or RETURNED_FOR_MODIFICATION.
         */
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

        /*
         * OLD_VALUE is always re-read fresh from the approved master
         * tables here on the server - never taken from a hidden form
         * field - so a stale or tampered "before" snapshot can never be
         * recorded.
         */
        ShareholderCreateRequest currentSnapshot =
                shareholderService.snapshotOf(folioBo);

        String oldValueJson =
                shareholderService.buildCreateProposalJson(currentSnapshot);

        String newValueJson =
                shareholderService.buildCreateProposalJson(edited);

        String changeId =
                workflowIdService.generateChangeId();

        /*
         * Create T_SHAREHOLDER_CHANGE_REQUEST.
         */
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

        /*
         * Generate the Approval Request ID.
         */
        Long requestId =
                workflowIdService.nextApprovalRequestId();

        /*
         * Current Bangladesh business date, assigned by the server.
         */
        LocalDate businessDate =
                LocalDate.now(ZoneId.of("Asia/Dhaka"));

        /*
         * Create T_APPROVAL_REQUEST.
         */
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
    public void resubmitReturnedModify(
            Long requestId,
            ShareholderCreateRequest edited,
            String makerId,
            String makerIp) {

        /*
         * Load the existing approval request.
         */
        ApprovalRequest approvalRequest =
                approvalRequestRepository
                        .findByRequestId(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Approval request not found: " + requestId
                                ));

        /*
         * This request must currently be returned to the Maker.
         */
        if (!"RETURNED_FOR_MODIFICATION".equals(
                approvalRequest.getStatus())) {

            throw new IllegalStateException(
                    "This request is not returned for modification."
            );
        }

        if (!"MAKER".equals(
                approvalRequest.getCurrentStage())) {

            throw new IllegalStateException(
                    "This request is not currently at Maker stage."
            );
        }

        /*
         * Only the original Maker may resubmit the request.
         */
        if (makerId == null ||
                !makerId.equals(approvalRequest.getMakerId())) {

            throw new IllegalStateException(
                    "Only the original maker can resubmit this request."
            );
        }

        /*
         * Get the original change request.
         */
        ShareholderChangeRequest changeRequest =
                changeRequestRepository
                        .findByChangeId(
                                approvalRequest.getSourceId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Shareholder change request not found."
                                ));

        if (!"SHAREHOLDER_UPDATE".equals(
                changeRequest.getOperationCode())) {

            throw new IllegalStateException(
                    "Only shareholder modification requests can be resubmitted."
            );
        }

        /*
         * The Folio is taken from the existing server-side request.
         * Never trust the browser for this value.
         */
        String folioBo =
                approvalRequest.getEntityId();

        if (folioBo == null || folioBo.isBlank()) {
            folioBo = changeRequest.getFolioBo();
        }

        if (folioBo == null || folioBo.isBlank()) {
            throw new IllegalStateException(
                    "Folio BO is missing from the request."
            );
        }

        /*
         * Force the server-side Folio into the edited proposal.
         */
        edited.getBasicInfo().setFolioBo(folioBo);

        /*
         * Make sure the shareholder still exists and obtain the
         * current approved snapshot.
         *
         * This also prevents resubmitting against a shareholder
         * that has disappeared or become invalid.
         */
        shareholderService.snapshotOf(folioBo);

        /*
         * Build the new proposal.
         *
         * IMPORTANT:
         * OLD_VALUE is NOT replaced.
         *
         * It remains the original snapshot that was stored when
         * the modification request was first created.
         */
        String newValueJson =
                shareholderService.buildCreateProposalJson(edited);

        /*
         * Update the existing change request.
         *
         * Same CHANGE_ID.
         * New version.
         */
        Integer changeVersion =
                changeRequest.getVersionNo();

        if (changeVersion == null) {
            changeVersion = 0;
        }

        changeRequest.setNewValue(newValueJson);
        changeRequest.setUpdatedAt(LocalDateTime.now());
        changeRequest.setVersionNo(changeVersion + 1);

        changeRequestRepository.save(changeRequest);

        /*
         * Move the SAME approval request back to Checker.
         *
         * We deliberately do NOT create a new REQUEST_ID.
         */
        approvalRequest.setStatus("PENDING_CHECKER");
        approvalRequest.setCurrentStage("CHECKER");

        /*
         * The previous checker is no longer the current actor
         * for this new checker decision.
         */
        approvalRequest.setCheckerId(null);
        approvalRequest.setCheckerIp(null);

        approvalRequest.setUpdatedAt(LocalDateTime.now());
        approvalRequest.setDecidedAt(null);

        /*
         * A resubmission is a new submission for today's
         * business date.
         */
        approvalRequest.setBusinessDate(
                LocalDate.now(ZoneId.of("Asia/Dhaka"))
        );

        Integer approvalVersion =
                approvalRequest.getVersionNo();

        if (approvalVersion == null) {
            approvalVersion = 0;
        }

        approvalRequest.setVersionNo(
                approvalVersion + 1
        );

        approvalRequestRepository.save(approvalRequest);

        /*
         * Append history.
         *
         * The previous RETURNED_FOR_MODIFICATION action remains
         * untouched.
         */
        Long actionId =
                workflowIdService.nextApprovalActionId();

        ApprovalAction approvalAction =
                new ApprovalAction();

        approvalAction.setActionId(actionId);
        approvalAction.setRequestId(requestId);
        approvalAction.setStage("MAKER");
        approvalAction.setAction("RESUBMITTED");
        approvalAction.setActorId(makerId);
        approvalAction.setActorIp(makerIp);
        approvalAction.setRemarks(null);
        approvalAction.setActionAt(LocalDateTime.now());

        approvalActionRepository.save(approvalAction);

        System.out.println("======================================");
        System.out.println("RETURNED REQUEST RESUBMITTED");
        System.out.println("======================================");
        System.out.println("Change ID      : " + changeRequest.getChangeId());
        System.out.println("Folio BO       : " + folioBo);
        System.out.println("Request ID     : " + requestId);
        System.out.println("Maker ID       : " + makerId);
        System.out.println("Operation      : SHAREHOLDER_UPDATE");
        System.out.println("Status         : PENDING_CHECKER");
        System.out.println("Current Stage  : CHECKER");
        System.out.println("Business Date  : "
                + approvalRequest.getBusinessDate());
        System.out.println("======================================");
    }


    public List<ApprovalRequest> getReturnedForModificationRequests(
            String makerId) {

        return approvalRequestRepository
                .findReturnedForModificationRequests(makerId);
    }


    public ShareholderCreateRequest getReturnedRequestData(
            Long requestId,
            String makerId) {

        ApprovalRequest approvalRequest =
                approvalRequestRepository
                        .findByRequestId(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Approval request not found: " + requestId
                                ));

        if (!"RETURNED_FOR_MODIFICATION".equals(
                approvalRequest.getStatus())) {

            throw new IllegalStateException(
                    "This request is not returned for modification."
            );
        }

        if (!"MAKER".equals(
                approvalRequest.getCurrentStage())) {

            throw new IllegalStateException(
                    "This request is not currently at Maker stage."
            );
        }

        if (!makerId.equals(
                approvalRequest.getMakerId())) {

            throw new IllegalStateException(
                    "You are not authorized to edit this request."
            );
        }

        ShareholderChangeRequest changeRequest =
                changeRequestRepository
                        .findByChangeId(
                                approvalRequest.getSourceId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Shareholder change request not found."
                                ));

        if (!"SHAREHOLDER_UPDATE".equals(
                changeRequest.getOperationCode())) {

            throw new IllegalStateException(
                    "Only shareholder modification requests can be edited."
            );
        }

        try {

            return objectMapper.readValue(
                    changeRequest.getNewValue(),
                    ShareholderCreateRequest.class
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to read returned shareholder proposal.",
                    e
            );
        }
    }



    public String getLatestReturnRemarks(Long requestId) {

        List<ApprovalAction> actions =
                approvalActionRepository
                        .findByRequestIdAndActionOrderByActionAtDesc(
                                requestId,
                                "RETURNED_FOR_MODIFICATION"
                        );

        if (actions.isEmpty()) {
            return "";
        }

        return actions.get(0).getRemarks();
    }





}