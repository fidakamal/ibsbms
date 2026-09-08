package com.example.ibsbms.controller;

import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.entity.ApprovalAction;
import com.example.ibsbms.entity.ShareholderChangeRequest;
import com.example.ibsbms.service.ApprovalWorkflowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ApprovalController {

    private final ApprovalWorkflowService approvalWorkflowService;

    public ApprovalController(
            ApprovalWorkflowService approvalWorkflowService) {

        this.approvalWorkflowService =
                approvalWorkflowService;
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

        List<ApprovalAction> history =
                approvalWorkflowService
                        .getApprovalHistory(requestId);

        model.addAttribute(
                "approvalRequest",
                approvalRequest
        );

        model.addAttribute(
                "changeRequest",
                changeRequest
        );

        model.addAttribute(
                "history",
                history
        );

        return "approval/approval-details";
    }

    @PostMapping("/approvals/{requestId}/approve")
    public String approve(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        try {

            /*
             * Temporary checker identity.
             *
             * Later this will come from authentication.
             */
            String checkerId = "test.checker";
            String checkerIp = "127.0.0.1";

            approvalWorkflowService.approve(
                    requestId,
                    checkerId,
                    checkerIp,
                    remarks
            );

            return "redirect:/approvals/"
                    + requestId
                    + "?success=Request approved successfully.";

        } catch (Exception e) {

            return "redirect:/approvals/"
                    + requestId
                    + "?error="
                    + e.getMessage();
        }
    }

    @PostMapping("/approvals/{requestId}/return")
    public String returnForModification(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        try {

            String checkerId = "test.checker";
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
            @RequestParam(required = false) String remarks) {

        try {

            String checkerId = "test.checker";
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
}