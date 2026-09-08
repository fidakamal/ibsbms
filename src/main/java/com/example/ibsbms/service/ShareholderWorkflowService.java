```java
package com.example.ibsbms.service;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.entity.ApprovalAction;
import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.repository.ApprovalActionRepository;
import com.example.ibsbms.repository.ApprovalRequestRepository;
import com.example.ibsbms.repository.ShareholderChangeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /*
     * ==========================================================
     * CREATE
     * ==========================================================
     */
    @Transactional
    public void submitCreateForApproval(
            ShareholderCreateRequest request,
            String makerId,
            String makerIp) {

        request.getBasicInfo().setFolioBo(null);

        String folioBo =
                workflowIdService.generateNextFolioBo();

        boolean alreadyReserved =
                changeRequestRepository
                        .existsByFolioBoAndOperationCode(
                                folioBo,
                                "SHAREHOLDER_CREATE"
                        );

        if (alreadyReserved) {
            throw new IllegalStateException(
                    "Generated Folio BO is already reserved: "
                            + folioBo
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
    }

    /*
     * ==========================================================
     * MODIFY
     * ==========================================================
     */
    @Transactional
    public void submitModifyForApproval(
            String folioBo,
            ShareholderCreateRequest edited,
            String makerId,
            String makerIp) {

        edited.getBasicInfo().setFolioBo(folioBo);

        boolean hasActiveRequest =
                !approvalRequestRepository
                        .findByBusinessRefAndStatusIn(
                                folioBo,
                                List.of(
                                        "PENDING_CHECKER",
                                        "RETURNED_FOR_MODIFICATION"
                                )
                        )
                        .isEmpty();

        if (hasActiveRequest) {
            throw new IllegalStateException(
                    "There is already a pending change request for Folio/BO "
                            + folioBo
            );
        }

        ShareholderCreateRequest currentSnapshot =
                shareholderService.snapshotOf(folioBo);

        String oldValueJson =
                shareholderService.buildCreateProposalJson(
                        currentSnapshot
                );

        String newValueJson =
                shareholderService.buildCreateProposalJson(
                        edited
                );

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
    }

    /*
     * ==========================================================
     * RETURNED REQUEST - RESUBMIT
     * ==========================================================
     *
     * Supports both:
     * - SHAREHOLDER_CREATE
     * - SHAREHOLDER_UPDATE
     *
     * A returned CREATE must NOT call snapshotOf(), because the
     * shareholder does not exist in the master tables yet.
     */
    @Transactional
    public void resubmitReturnedModify(
            Long requestId,
            ShareholderCreateRequest edited,
            String makerId,
            String makerIp) {

        ApprovalRequest approvalRequest =
                approvalRequestRepository
                        .findByRequestId(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Approval request not found: "
                                                + requestId
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

        if (makerId == null ||
                !makerId.equals(
                        approvalRequest.getMakerId())) {

            throw new IllegalStateException(
                    "Only the original maker can resubmit this request."
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

        String operationCode =
                changeRequest.getOperationCode();

        if (!"SHAREHOLDER_CREATE".equals(operationCode) &&
                !"SHAREHOLDER_UPDATE".equals(operationCode)) {

            throw new IllegalStateException(
                    "Only shareholder Create or Modify requests can be resubmitted."
            );
        }

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

        edited.getBasicInfo().setFolioBo(folioBo);

        /*
         * CREATE:
         *
         * No approved shareholder exists yet.
         * Do not call snapshotOf().
         */
        if ("SHAREHOLDER_CREATE".equals(operationCode)) {

            // Nothing else is required here.
        }

        /*
         * UPDATE:
         *
         * The approved shareholder must still exist.
         */
        if ("SHAREHOLDER_UPDATE".equals(operationCode)) {

            shareholderService.snapshotOf(folioBo);
        }

        String newValueJson =
                shareholderService.buildCreateProposalJson(
                        edited
                );

        Integer changeVersion =
                changeRequest.getVersionNo();

        if (changeVersion == null) {
            changeVersion = 0;
        }

        changeRequest.setNewValue(newValueJson);
        changeRequest.setUpdatedAt(LocalDateTime.now());
        changeRequest.setVersionNo(
                changeVersion + 1
        );

        changeRequestRepository.save(changeRequest);

        /*
         * Reuse the SAME approval request.
         */
        approvalRequest.setStatus("PENDING_CHECKER");
        approvalRequest.setCurrentStage("CHECKER");

        approvalRequest.setCheckerId(null);
        approvalRequest.setCheckerIp(null);

        approvalRequest.setUpdatedAt(LocalDateTime.now());
        approvalRequest.setDecidedAt(null);

        approvalRequest.setBusinessDate(
                LocalDate.now(
                        ZoneId.of("Asia/Dhaka")
                )
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
         * Keep the previous RETURNED_FOR_MODIFICATION action
         * and append a new RESUBMITTED action.
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
        System.out.println("Change ID      : "
                + changeRequest.getChangeId());
        System.out.println("Folio BO       : "
                + folioBo);
        System.out.println("Request ID     : "
                + requestId);
        System.out.println("Maker ID       : "
                + makerId);
        System.out.println("Operation      : "
                + operationCode);
        System.out.println("Status         : PENDING_CHECKER");
        System.out.println("Current Stage  : CHECKER");
        System.out.println("Business Date  : "
                + approvalRequest.getBusinessDate());
        System.out.println("======================================");
    }

    /*
     * ==========================================================
     * RETURNED REQUEST LIST
     * ==========================================================
     */
    public List<ApprovalRequest> getReturnedForModificationRequests(
            String makerId) {

        return approvalRequestRepository
                .findReturnedForModificationRequests(
                        makerId
                );
    }

    /*
     * ==========================================================
     * LOAD RETURNED REQUEST DATA
     * ==========================================================
     */
    public ShareholderCreateRequest getReturnedRequestData(
            Long requestId,
            String makerId) {

        ApprovalRequest approvalRequest =
                approvalRequestRepository
                        .findByRequestId(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Approval request not found: "
                                                + requestId
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

        if (!"SHAREHOLDER_CREATE".equals(
                changeRequest.getOperationCode()) &&
                !"SHAREHOLDER_UPDATE".equals(
                        changeRequest.getOperationCode())) {

            throw new IllegalStateException(
                    "Only shareholder Create or Modify requests can be edited."
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

    /*
     * ==========================================================
     * LATEST RETURN REMARKS
     * ==========================================================
     */
    public String getLatestReturnRemarks(
            Long requestId) {

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
```
