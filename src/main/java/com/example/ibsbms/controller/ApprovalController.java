package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.entity.ApprovalAction;
import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.repository.ApprovalActionRepository;
import com.example.ibsbms.repository.ApprovalRequestRepository;
import com.example.ibsbms.repository.ShareholderChangeRequestRepository;
import com.example.ibsbms.service.ShareholderService;
import com.example.ibsbms.service.ShareholderWorkflowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
public class ApprovalController {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalActionRepository approvalActionRepository;
    private final ShareholderChangeRequestRepository changeRequestRepository;
    private final ShareholderWorkflowService shareholderWorkflowService;
    private final ShareholderService shareholderService;

    public ApprovalController(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalActionRepository approvalActionRepository,
            ShareholderChangeRequestRepository changeRequestRepository,
            ShareholderWorkflowService shareholderWorkflowService,
            ShareholderService shareholderService) {

        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalActionRepository = approvalActionRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.shareholderWorkflowService = shareholderWorkflowService;
        this.shareholderService = shareholderService;
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {

        List<ApprovalRequest> requests = approvalRequestRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ApprovalRequest::getCreatedAt).reversed())
                .toList();

        long pendingCount = requests.stream()
                .filter(r -> "PENDING_CHECKER".equals(r.getStatus()))
                .count();

        model.addAttribute("requests", requests);
        model.addAttribute("pendingCount", pendingCount);

        return "approval/approval-list";
    }

    @GetMapping("/approvals/{requestId}")
    public String approvalDetails(
            @PathVariable Long requestId,
            Model model) {

        ApprovalRequest approvalRequest = approvalRequestRepository
                .findById(requestId)
                .orElse(null);

        if (approvalRequest == null) {
            return "redirect:/approvals?error=Approval request not found: " + requestId;
        }

        List<ApprovalAction> history = approvalActionRepository
                .findByRequestIdOrderByActionAtAsc(requestId);
        ShareholderCreateRequest newData = new ShareholderCreateRequest();
        ShareholderCreateRequest oldData = null;

        ShareholderChangeRequest changeRequest = changeRequestRepository
                .findById(approvalRequest.getSourceId())
                .orElse(null);

        if (changeRequest != null) {
            newData = shareholderService.parseProposalJson(changeRequest.getNewValue());

            if (changeRequest.getOldValue() != null) {
                oldData = shareholderService.parseProposalJson(changeRequest.getOldValue());
            }
        }

        model.addAttribute("requestId", requestId);
        model.addAttribute("approvalRequest", approvalRequest);
        model.addAttribute("status", approvalRequest.getStatus());
        model.addAttribute("history", history);
        model.addAttribute("newData", newData);
        model.addAttribute("oldData", oldData);

        return "approval/approval-details";
    }


    @PostMapping("/approvals/{requestId}/approve")
    public String approve(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        /*
         * Temporary checker identity.
         * Later this will come from authentication/session.
         */
        String checkerId = "test.checker";
        String checkerIp = "127.0.0.1";

        try {
            shareholderWorkflowService.approve(requestId, checkerId, checkerIp, remarks);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/approvals/" + requestId + "?error=" + e.getMessage();
        }

        return "redirect:/approvals/" + requestId
                + "?success=Request approved successfully.";
    }


    @PostMapping("/approvals/{requestId}/return")
    public String returnForModification(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        String checkerId = "test.checker";
        String checkerIp = "127.0.0.1";

        try {
            shareholderWorkflowService.returnForModification(requestId, checkerId, checkerIp, remarks);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/approvals/" + requestId + "?error=" + e.getMessage();
        }

        return "redirect:/approvals/" + requestId
                + "?success=Request returned to maker for modification.";
    }


    @PostMapping("/approvals/{requestId}/reject")
    public String reject(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        /*
         * Temporary checker identity.
         * Later this will come from authentication/session.
         */
        String checkerId = "test.checker";
        String checkerIp = "127.0.0.1";

        try {
            shareholderWorkflowService.reject(requestId, checkerId, checkerIp, remarks);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/approvals/" + requestId + "?error=" + e.getMessage();
        }

        return "redirect:/approvals/" + requestId
                + "?success=Request rejected successfully.";
    }
}