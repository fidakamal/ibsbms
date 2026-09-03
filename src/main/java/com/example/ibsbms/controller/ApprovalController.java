package com.example.ibsbms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ApprovalController {

    /*
     * Temporary in-memory approval state.
     *
     * Later this will be replaced by:
     * T_APPROVAL_REQUEST
     * T_APPROVAL_ACTION
     */
    private final Map<Long, String> approvalStatuses = new HashMap<>();

    @GetMapping("/approvals")
    public String approvals(Model model) {

        // Temporary mock data
        model.addAttribute("pendingCount", 3);

        return "approval/approval-list";
    }

    @GetMapping("/approvals/{requestId}")
    public String approvalDetails(
            @PathVariable Long requestId,
            Model model) {

        String status = approvalStatuses.getOrDefault(
                requestId,
                "PENDING_CHECKER"
        );

        model.addAttribute("requestId", requestId);
        model.addAttribute("status", status);

        return "approval/approval-details";
    }


    @PostMapping("/approvals/{requestId}/approve")
    public String approve(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        String currentStatus = approvalStatuses.getOrDefault(
                requestId,
                "PENDING_CHECKER"
        );

        if (!"PENDING_CHECKER".equals(currentStatus)) {
            return "redirect:/approvals/" + requestId
                    + "?error=Request is no longer pending.";
        }

        /*
         * Temporary checker identity.
         * Later this will come from authentication/session.
         */
        String makerId = "test.maker";
        String checkerId = "test.checker";

        // Maker and checker must be different.
        if (makerId.equals(checkerId)) {
            return "redirect:/approvals/" + requestId
                    + "?error=Maker cannot approve their own request.";
        }

        approvalStatuses.put(
                requestId,
                "APPROVED"
        );

        return "redirect:/approvals/" + requestId
                + "?success=Request approved successfully.";
    }


    @PostMapping("/approvals/{requestId}/return")
    public String returnForModification(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        String currentStatus = approvalStatuses.getOrDefault(
                requestId,
                "PENDING_CHECKER"
        );

        if (!"PENDING_CHECKER".equals(currentStatus)) {
            return "redirect:/approvals/" + requestId
                    + "?error=Request is no longer pending.";
        }

        if (remarks == null || remarks.trim().isEmpty()) {
            return "redirect:/approvals/" + requestId
                    + "?error=Remarks are required when returning a request.";
        }

        approvalStatuses.put(
                requestId,
                "RETURNED_FOR_MODIFICATION"
        );

        return "redirect:/approvals/" + requestId
                + "?success=Request returned to maker for modification.";
    }


    @PostMapping("/approvals/{requestId}/reject")
    public String reject(
            @PathVariable Long requestId,
            @RequestParam(required = false) String remarks) {

        String currentStatus = approvalStatuses.getOrDefault(
                requestId,
                "PENDING_CHECKER"
        );

        if (!"PENDING_CHECKER".equals(currentStatus)) {
            return "redirect:/approvals/" + requestId
                    + "?error=Request is no longer pending.";
        }

        if (remarks == null || remarks.trim().isEmpty()) {
            return "redirect:/approvals/" + requestId
                    + "?error=Remarks are required when rejecting a request.";
        }

        approvalStatuses.put(
                requestId,
                "REJECTED"
        );

        return "redirect:/approvals/" + requestId
                + "?success=Request rejected successfully.";
    }
}