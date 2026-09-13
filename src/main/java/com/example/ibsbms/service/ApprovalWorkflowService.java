package com.example.ibsbms.service;

import com.example.ibsbms.entity.ApprovalAction;
import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.entity.BusinessAudit;
import com.example.ibsbms.entity.ShareAddress;
import com.example.ibsbms.entity.ShareBankInfo;
import com.example.ibsbms.entity.Shareholder;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.repository.ApprovalActionRepository;
import com.example.ibsbms.repository.ApprovalRequestRepository;
import com.example.ibsbms.repository.BusinessAuditRepository;
import com.example.ibsbms.repository.ShareAddressRepository;
import com.example.ibsbms.repository.ShareBankInfoRepository;
import com.example.ibsbms.repository.ShareholderChangeRequestRepository;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
    private final ShareholderRepository shareholderRepository;
    private final ShareAddressRepository shareAddressRepository;
    private final ShareBankInfoRepository shareBankInfoRepository;
    private final BusinessAuditRepository businessAuditRepository;
    private final WorkflowIdService workflowIdService;
    private final ObjectMapper objectMapper;

    public ApprovalWorkflowService(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalActionRepository approvalActionRepository,
            ShareholderChangeRequestRepository changeRequestRepository,
            ShareholderRepository shareholderRepository,
            ShareAddressRepository shareAddressRepository,
            ShareBankInfoRepository shareBankInfoRepository,
            BusinessAuditRepository businessAuditRepository,
            WorkflowIdService workflowIdService,
            ObjectMapper objectMapper) {

        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalActionRepository = approvalActionRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.shareholderRepository = shareholderRepository;
        this.shareAddressRepository = shareAddressRepository;
        this.shareBankInfoRepository = shareBankInfoRepository;
        this.businessAuditRepository = businessAuditRepository;
        this.workflowIdService = workflowIdService;
        this.objectMapper = objectMapper;
    }

    public List<ApprovalRequest> getPendingCheckerRequests() {
        return approvalRequestRepository.findPendingCheckerRequests();
    }

    public List<ApprovalRequest> getRejectedRequests() {
        return approvalRequestRepository.findRejectedRequests();
    }

    public String getLatestRejectionRemarks(Long requestId) {

        List<ApprovalAction> actions =
                approvalActionRepository
                        .findByRequestIdAndActionOrderByActionAtDesc(
                                requestId,
                                "REJECTED"
                        );

        if (actions.isEmpty()) {
            return "";
        }

        return actions.get(0).getRemarks();
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

    public List<ApprovalAction> getApprovalHistory(Long requestId) {
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

        if (approvalRequest.getMakerId().equals(checkerId)) {
            throw new IllegalStateException(
                    "Maker cannot approve their own request."
            );
        }

        validateBusinessDate(approvalRequest);

        ShareholderChangeRequest changeRequest =
                getChangeRequest(approvalRequest);

        if ("SHAREHOLDER_UPDATE".equals(
                changeRequest.getOperationCode())) {

            approveModify(
                    approvalRequest,
                    changeRequest,
                    checkerId,
                    checkerIp,
                    remarks
            );

            return;
        }

        if (!"SHAREHOLDER_CREATE".equals(
                changeRequest.getOperationCode())) {

            throw new IllegalStateException(
                    "Unsupported shareholder operation: "
                            + changeRequest.getOperationCode()
            );
        }

        JsonNode proposal;

        try {
            proposal =
                    objectMapper.readTree(
                            changeRequest.getNewValue()
                    );
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Unable to read shareholder proposal JSON.",
                    e
            );
        }

        JsonNode basicInfo =
                proposal.path("basicInfo");

        JsonNode address =
                proposal.path("address");

        JsonNode bankInfo =
                proposal.path("bankInfo");

        String folioBo =
                basicInfo.path("folioBo").asText("");

        if (folioBo.isBlank()) {
            throw new IllegalStateException(
                    "Folio BO is missing from the proposal."
            );
        }

        /*
         * Generate OIDs using the confirmed MAX + 1 rule.
         */
        String accountOid =
                workflowIdService.generateNextAccountShareOid();

        String addressOid =
                workflowIdService.generateNextAddressShareOid();

        /*
         * Create final shareholder record.
         */
        Shareholder shareholder =
                new Shareholder();

        shareholder.setOid(accountOid);
        shareholder.setFolioBo(folioBo);

        shareholder.setCustName(
                basicInfo.path("custName").asText(null)
        );

        shareholder.setFatherName(
                basicInfo.path("fatherName").asText(null)
        );

        shareholder.setMotherName(
                basicInfo.path("motherName").asText(null)
        );

        shareholder.setSpouseName(
                basicInfo.path("spouseName").asText(null)
        );

        shareholder.setRepresentative(
                basicInfo.path("representative").asText(null)
        );

        shareholder.setCustType(
                nullableInteger(basicInfo, "custType")
        );

        shareholder.setCitizenType(
                nullableInteger(basicInfo, "citizenType")
        );

        shareholder.setResidentType(
                basicInfo.path("residentType").asText(null)
        );

        shareholder.setPhone(
                basicInfo.path("phone").asText(null)
        );

        shareholder.setEmail(
                basicInfo.path("email").asText(null)
        );

        shareholder.setDob(
                nullableDate(basicInfo, "dob")
        );

        /*
         * Registration date is controlled by the server,
         * not the browser.
         */
        shareholder.setRegistrationDate(
                LocalDate.now(ZoneId.of("Asia/Dhaka"))
        );

        shareholder.setIsValid(1);

        shareholder.setIsEmployee(
                nullableIntegerOrDefault(
                        basicInfo,
                        "isEmployee",
                        0
                )
        );

        shareholder.setNidNo(
                basicInfo.path("nidNo").asText(null)
        );

        shareholder.setTinNo(
                basicInfo.path("tinNo").asText(null)
        );

        shareholder.setIcbCode(
                nullableIntegerOrDefault(
                        basicInfo,
                        "icbCode",
                        0
                )
        );

        /*
         * Create starts with no lien.
         */
        shareholder.setIsLien(0);

        /*
         * These values must never come from maker JSON.
         */
        shareholder.setShares(0L);
        shareholder.setSuspense(0L);
        shareholder.setBonus(0L);
        shareholder.setBalance(0L);

        shareholder.setMakerId(
                approvalRequest.getMakerId()
        );

        shareholder.setCheckerId(
                checkerId
        );

        /*
         * Confirmed approved shareholder status.
         */
        shareholder.setStatus(1);

        shareholder.setOldBo(null);

        shareholderRepository.save(shareholder);

        /*
         * Create final address record.
         */
        ShareAddress shareAddress =
                new ShareAddress();

        shareAddress.setOid(addressOid);
        shareAddress.setFolioBo(folioBo);

        shareAddress.setAdd1(
                address.path("add1").asText(null)
        );

        shareAddress.setAdd2(
                address.path("add2").asText(null)
        );

        shareAddress.setAdd3(
                address.path("add3").asText(null)
        );

        shareAddress.setAdd4(
                address.path("add4").asText(null)
        );

        shareAddress.setCountryName(
                address.path("countryName").asText(null)
        );

        shareAddressRepository.save(shareAddress);

        /*
         * Bank information is optional.
         *
         * We only insert a bank row if at least one bank
         * field was supplied.
         */
        if (hasBankInformation(bankInfo)) {

            String bankOid =
                    workflowIdService
                            .generateNextBankInfoShareOid();

            ShareBankInfo shareBankInfo =
                    new ShareBankInfo();

            shareBankInfo.setOid(bankOid);
            shareBankInfo.setFolioBo(folioBo);

            shareBankInfo.setAccNo(
                    bankInfo.path("accNo").asText(null)
            );

            shareBankInfo.setBankName(
                    bankInfo.path("bankName").asText(null)
            );

            shareBankInfo.setBranchName(
                    bankInfo.path("branchName").asText(null)
            );

            shareBankInfo.setRoutingNo(
                    bankInfo.path("routingNo").asText(null)
            );

            shareBankInfoRepository.save(shareBankInfo);
        }

        /*
         * Update approval request.
         */
        LocalDateTime now =
                LocalDateTime.now();

        approvalRequest.setStatus(APPROVED);
        approvalRequest.setCurrentStage(COMPLETED);
        approvalRequest.setCheckerId(checkerId);
        approvalRequest.setCheckerIp(checkerIp);
        approvalRequest.setUpdatedAt(now);
        approvalRequest.setDecidedAt(now);

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
         * Append approval action.
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
         * Business audit.
         *
         * For CREATE, the complete proposed snapshot is
         * recorded as the new value.
         */
        Long auditId =
                workflowIdService.nextBusinessAuditId();

        BusinessAudit audit =
                new BusinessAudit();

        audit.setAuditId(auditId);
        audit.setEventTime(now);
        audit.setModuleCode("SHAREHOLDER");
        audit.setActionType("CREATE");
        audit.setEntityType("SHAREHOLDER");
        audit.setEntityId(folioBo);
        audit.setBusinessRef(folioBo);

        audit.setChangedFields("""
            ["basicInfo","address","bankInfo"]
            """);

        audit.setOldValue(null);
        audit.setNewValue(changeRequest.getNewValue());

        audit.setActorId(checkerId);
        audit.setClientIp(checkerIp);
        audit.setClientPcName(null);
        audit.setUserAgent(null);

        audit.setApprovalRequestId(requestId);
        audit.setCorrelationId(
                changeRequest.getChangeId()
        );

        audit.setRemarks(remarks);

        businessAuditRepository.save(audit);
    }



    private void approveModify(
            ApprovalRequest approvalRequest,
            ShareholderChangeRequest changeRequest,
            String checkerId,
            String checkerIp,
            String remarks) {

        JsonNode oldProposal;
        JsonNode newProposal;

        try {

            oldProposal =
                    objectMapper.readTree(
                            changeRequest.getOldValue()
                    );

            newProposal =
                    objectMapper.readTree(
                            changeRequest.getNewValue()
                    );

        } catch (JacksonException e) {

            throw new IllegalStateException(
                    "Unable to read shareholder modification JSON.",
                    e
            );
        }

        JsonNode oldBasicInfo =
                oldProposal.path("basicInfo");

        JsonNode newBasicInfo =
                newProposal.path("basicInfo");

        JsonNode oldAddress =
                oldProposal.path("address");

        JsonNode newAddress =
                newProposal.path("address");

        JsonNode oldBankInfo =
                oldProposal.path("bankInfo");

        JsonNode newBankInfo =
                newProposal.path("bankInfo");


        /*
         * The Folio BO is controlled by the workflow request.
         * Do not trust a modified Folio BO from maker JSON.
         */
        String folioBo =
                approvalRequest.getEntityId();

        if (folioBo == null || folioBo.isBlank()) {

            folioBo =
                    changeRequest.getFolioBo();
        }

        if (folioBo == null || folioBo.isBlank()) {

            throw new IllegalStateException(
                    "Folio BO is missing from the modification request."
            );
        }

        final String approvedFolioBo = folioBo;


        /*
         * Load the existing approved shareholder.
         */
        Shareholder shareholder =
                shareholderRepository
                        .findByFolioBoAndIsValid(
                                approvedFolioBo,
                                1
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Approved shareholder not found for Folio BO: "
                                                + approvedFolioBo
                                ));


        /*
         * Update ONLY permitted shareholder fields.
         *
         * Financial/holding fields are deliberately not touched:
         *
         * SHARES
         * SUSPENSE
         * BONUS
         * BALANCE
         */

        shareholder.setCustName(
                newBasicInfo.path("custName").asText(null)
        );

        shareholder.setFatherName(
                newBasicInfo.path("fatherName").asText(null)
        );

        shareholder.setMotherName(
                newBasicInfo.path("motherName").asText(null)
        );

        shareholder.setSpouseName(
                newBasicInfo.path("spouseName").asText(null)
        );

        shareholder.setRepresentative(
                newBasicInfo.path("representative").asText(null)
        );

        shareholder.setCustType(
                nullableInteger(
                        newBasicInfo,
                        "custType"
                )
        );

        shareholder.setCitizenType(
                nullableInteger(
                        newBasicInfo,
                        "citizenType"
                )
        );

        shareholder.setResidentType(
                newBasicInfo.path("residentType").asText(null)
        );

        shareholder.setPhone(
                newBasicInfo.path("phone").asText(null)
        );

        shareholder.setEmail(
                newBasicInfo.path("email").asText(null)
        );

        shareholder.setDob(
                nullableDate(
                        newBasicInfo,
                        "dob"
                )
        );

        shareholder.setIsEmployee(
                nullableIntegerOrDefault(
                        newBasicInfo,
                        "isEmployee",
                        0
                )
        );

        shareholder.setNidNo(
                newBasicInfo.path("nidNo").asText(null)
        );

        shareholder.setTinNo(
                newBasicInfo.path("tinNo").asText(null)
        );

        shareholder.setIcbCode(
                nullableIntegerOrDefault(
                        newBasicInfo,
                        "icbCode",
                        0
                )
        );

        shareholder.setCheckerId(checkerId);

        shareholderRepository.save(shareholder);


        /*
         * Update Address.
         */
        ShareAddress shareAddress =
                shareAddressRepository
                        .findByFolioBo(folioBo)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Address record not found for Folio BO: "
                                                + approvedFolioBo
                                ));

        shareAddress.setAdd1(
                newAddress.path("add1").asText(null)
        );

        shareAddress.setAdd2(
                newAddress.path("add2").asText(null)
        );

        shareAddress.setAdd3(
                newAddress.path("add3").asText(null)
        );

        shareAddress.setAdd4(
                newAddress.path("add4").asText(null)
        );

        shareAddress.setCountryName(
                newAddress.path("countryName").asText(null)
        );

        shareAddressRepository.save(shareAddress);


        /*
         * Bank information is optional.
         */
        if (hasBankInformation(newBankInfo)) {

            ShareBankInfo shareBankInfo =
                    shareBankInfoRepository
                            .findByFolioBo(folioBo)
                            .orElse(null);

            /*
             * No existing bank record.
             * Create one.
             */
            if (shareBankInfo == null) {

                String bankOid =
                        workflowIdService
                                .generateNextBankInfoShareOid();

                shareBankInfo =
                        new ShareBankInfo();

                shareBankInfo.setOid(bankOid);
                shareBankInfo.setFolioBo(folioBo);
            }

            shareBankInfo.setAccNo(
                    newBankInfo.path("accNo").asText(null)
            );

            shareBankInfo.setBankName(
                    newBankInfo.path("bankName").asText(null)
            );

            shareBankInfo.setBranchName(
                    newBankInfo.path("branchName").asText(null)
            );

            shareBankInfo.setRoutingNo(
                    newBankInfo.path("routingNo").asText(null)
            );

            shareBankInfoRepository.save(shareBankInfo);
        }


        /*
         * Calculate changed fields for the business audit.
         */
        String changedFields =
                buildChangedFields(
                        oldProposal,
                        newProposal
                );


        /*
         * Update approval request.
         */
        LocalDateTime now =
                LocalDateTime.now();

        approvalRequest.setStatus(APPROVED);
        approvalRequest.setCurrentStage(COMPLETED);
        approvalRequest.setCheckerId(checkerId);
        approvalRequest.setCheckerIp(checkerIp);
        approvalRequest.setUpdatedAt(now);
        approvalRequest.setDecidedAt(now);

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
         * Append approval action.
         */
        saveAction(
                approvalRequest.getRequestId(),
                CHECKER,
                "APPROVED",
                checkerId,
                checkerIp,
                remarks
        );


        /*
         * Business audit.
         */
        Long auditId =
                workflowIdService.nextBusinessAuditId();

        BusinessAudit audit =
                new BusinessAudit();

        audit.setAuditId(auditId);
        audit.setEventTime(now);
        audit.setModuleCode("SHAREHOLDER");
        audit.setActionType("MODIFY");
        audit.setEntityType("SHAREHOLDER");
        audit.setEntityId(folioBo);
        audit.setBusinessRef(folioBo);

        audit.setChangedFields(
                changedFields
        );

        audit.setOldValue(
                changeRequest.getOldValue()
        );

        audit.setNewValue(
                changeRequest.getNewValue()
        );

        audit.setActorId(checkerId);
        audit.setClientIp(checkerIp);
        audit.setClientPcName(null);
        audit.setUserAgent(null);

        audit.setApprovalRequestId(
                approvalRequest.getRequestId()
        );

        audit.setCorrelationId(
                changeRequest.getChangeId()
        );

        audit.setRemarks(remarks);

        businessAuditRepository.save(audit);
    }



    private String buildChangedFields(
            JsonNode oldProposal,
            JsonNode newProposal) {

        List<String> changedFields =
                new java.util.ArrayList<>();

        addChangedFields(
                changedFields,
                oldProposal.path("basicInfo"),
                newProposal.path("basicInfo"),
                "basicInfo"
        );

        addChangedFields(
                changedFields,
                oldProposal.path("address"),
                newProposal.path("address"),
                "address"
        );

        addChangedFields(
                changedFields,
                oldProposal.path("bankInfo"),
                newProposal.path("bankInfo"),
                "bankInfo"
        );

        try {

            return objectMapper.writeValueAsString(
                    changedFields
            );

        } catch (JacksonException e) {

            throw new IllegalStateException(
                    "Unable to create changed fields audit.",
                    e
            );
        }
    }



    private void addChangedFields(
            List<String> changedFields,
            JsonNode oldNode,
            JsonNode newNode,
            String section) {

        java.util.Iterator<String> fields =
                newNode.propertyNames().iterator();

        while (fields.hasNext()) {

            String field =
                    fields.next();

            JsonNode oldValue =
                    oldNode.path(field);

            JsonNode newValue =
                    newNode.path(field);

            if (!oldValue.equals(newValue)) {

                changedFields.add(
                        section + "." + field
                );
            }
        }
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

        Integer version =
                approvalRequest.getVersionNo();

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

        Integer version =
                approvalRequest.getVersionNo();

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

    private Integer nullableInteger(
            JsonNode node,
            String field) {

        JsonNode value = node.path(field);

        if (value.isMissingNode() ||
                value.isNull() ||
                value.asText().isBlank()) {

            return null;
        }

        return value.asInt();
    }

    private Integer nullableIntegerOrDefault(
            JsonNode node,
            String field,
            int defaultValue) {

        Integer value =
                nullableInteger(node, field);

        return value == null
                ? defaultValue
                : value;
    }

    private LocalDate nullableDate(
            JsonNode node,
            String field) {

        JsonNode value = node.path(field);

        if (value.isMissingNode() ||
                value.isNull() ||
                value.asText().isBlank()) {

            return null;
        }

        return LocalDate.parse(value.asText());
    }

    private boolean hasBankInformation(
            JsonNode bankInfo) {

        return hasText(bankInfo, "accNo")
                || hasText(bankInfo, "bankName")
                || hasText(bankInfo, "branchName")
                || hasText(bankInfo, "routingNo");
    }

    private boolean hasText(
            JsonNode node,
            String field) {

        return !node.path(field)
                .asText("")
                .trim()
                .isEmpty();
    }


    public List<ApprovalRequest> getMakerCreateRequests(String makerId) {
        return approvalRequestRepository.findMakerCreateRequests(makerId);
    }


    public List<ApprovalRequest> getReturnedForModificationRequests() {
        return approvalRequestRepository
                .findReturnedForModificationRequests();
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

