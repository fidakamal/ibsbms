package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.entity.ApprovalAction;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.service.ApprovalWorkflowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.ibsbms.service.ShareholderService;

import java.security.Principal;
import java.util.List;

@Controller
public class ApprovalController {

    private final ApprovalWorkflowService approvalWorkflowService;
    private final ShareholderService shareholderService;

    public ApprovalController(
            ApprovalWorkflowService approvalWorkflowService,
            ShareholderService shareholderService) {

        this.approvalWorkflowService = approvalWorkflowService;
        this.shareholderService = shareholderService;
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {

        List<ApprovalRequest> requests =
                approvalWorkflowService
                        .getPendingCheckerRequests();

        model.addAttribute(
                "approvalRequests",
                requests
        );

        model.addAttribute(
                "pendingCount",
                requests.size()
        );

        return "approval/approval-list";
    }

    @GetMapping("/approvals/{requestId}")
    public String approvalDetails(
            @PathVariable Long requestId,
            Model model) {

        ApprovalRequest approvalRequest =
                approvalWorkflowService
                        .getApprovalRequest(requestId);

        ShareholderChangeRequest changeRequest =
                approvalWorkflowService
                        .getChangeRequest(approvalRequest);

        ShareholderCreateRequest proposal =
                shareholderService.parseProposalJson(
                        changeRequest.getNewValue());

        List<ApprovalAction> history =
                approvalWorkflowService
                        .getApprovalHistory(requestId);

        model.addAttribute(
                "requestId",
                requestId
        );

        model.addAttribute(
                "status",
                approvalRequest.getStatus()
        );

        model.addAttribute(
                "approvalRequest",
                approvalRequest
        );

        model.addAttribute(
                "changeRequest",
                changeRequest
        );

        model.addAttribute(
                "proposal",
                proposal
        );

        model.addAttribute(
                "history",
                history
        );

        return "approval/approval-details";
    }

    @GetMapping("/approvals/rejected")
    public String rejectedRequests(Model model) {

        List<ApprovalRequest> requests =
                approvalWorkflowService.getRejectedRequests();

        java.util.Map<Long, String> rejectionRemarks =
                new java.util.HashMap<>();

        for (ApprovalRequest request : requests) {

            rejectionRemarks.put(
                    request.getRequestId(),
                    approvalWorkflowService
                            .getLatestRejectionRemarks(
                                    request.getRequestId()
                            )
            );
        }

        model.addAttribute(
                "rejectedRequests",
                requests
        );

        model.addAttribute(
                "rejectedCount",
                requests.size()
        );

        model.addAttribute(
                "rejectionRemarks",
                rejectionRemarks
        );

        return "approval/rejected-list";
    }


    @PostMapping("/approvals/{requestId}/approve")
    public String approve(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            Principal principal) {

        try {

            String checkerId = principal.getName();
            String checkerIp = "127.0.0.1";

            approvalWorkflowService.approve(
                    requestId,
                    checkerId,
                    checkerIp,
                    remarks
            );

            return "redirect:/shareholders";

        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/approvals/"
                    + requestId
                    + "?error="
                    + e.getMessage();
        }
    }



    @PostMapping("/approvals/{requestId}/return")
    public String returnForModification(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            Principal principal) {

        try {

            String checkerId = principal.getName();
            String checkerIp = "127.0.0.1";

            approvalWorkflowService.returnForModification(
                    requestId,
                    checkerId,
                    checkerIp,
                    remarks
            );

            return "redirect:/approvals/"
                    + requestId
                    + "?success=Request returned to maker for modification.";

        } catch (Exception e) {

            return "redirect:/approvals/"
                    + requestId
                    + "?error="
                    + e.getMessage();
        }
    }

    @PostMapping("/approvals/{requestId}/reject")
    public String reject(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks,
            Principal principal) {

        try {

            String checkerId = principal.getName();
            String checkerIp = "127.0.0.1";

            approvalWorkflowService.reject(
                    requestId,
                    checkerId,
                    checkerIp,
                    remarks
            );

            return "redirect:/approvals/"
                    + requestId
                    + "?success=Request rejected successfully.";

        } catch (Exception e) {

            return "redirect:/approvals/"
                    + requestId
                    + "?error="
                    + e.getMessage();
        }
    }


    @GetMapping("/my-requests")
    public String myRequests(
            Principal principal,
            Model model) {

        String makerId = principal.getName();

        System.out.println("======================================");
        System.out.println("MY REQUESTS");
        System.out.println("Logged-in user: " + makerId);

        List<ApprovalRequest> requests =
                approvalWorkflowService
                        .getMakerCreateRequests(makerId);

        System.out.println("Number of requests: " + requests.size());

        for (ApprovalRequest request : requests) {
            System.out.println(
                    "Request ID: " + request.getRequestId()
                            + " | Maker ID: " + request.getMakerId()
                            + " | Operation: " + request.getOperationCode()
                            + " | Status: " + request.getStatus()
            );
        }

        System.out.println("======================================");

        model.addAttribute(
                "requests",
                requests
        );

        return "approval/my-requests";
    }
}