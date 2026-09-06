package com.example.ibsbms.service;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.repository.ShareholderChangeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ShareholderWorkflowService {

    private final ShareholderService shareholderService;
    private final WorkflowIdService workflowIdService;
    private final ShareholderChangeRequestRepository changeRequestRepository;

    public ShareholderWorkflowService(
            ShareholderService shareholderService,
            WorkflowIdService workflowIdService,
            ShareholderChangeRequestRepository changeRequestRepository) {

        this.shareholderService = shareholderService;
        this.workflowIdService = workflowIdService;
        this.changeRequestRepository = changeRequestRepository;
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
         * Create the shareholder change request.
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

        System.out.println("======================================");
        System.out.println("CHANGE REQUEST SAVED");
        System.out.println("======================================");
        System.out.println("Change ID : " + changeId);
        System.out.println("Folio BO  : " + folioBo);
        System.out.println("Maker ID  : " + makerId);
        System.out.println("Operation : SHAREHOLDER_CREATE");
        System.out.println("--------------------------------------");
        System.out.println(proposalJson);
        System.out.println("======================================");
    }
}