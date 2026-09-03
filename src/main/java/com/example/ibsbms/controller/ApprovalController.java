package com.example.ibsbms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ApprovalController {

    @GetMapping("/approvals")
    public String approvals(Model model) {

        // Temporary mock data.
        // Later this will come from T_APPROVAL_REQUEST.

        model.addAttribute("pendingCount", 3);

        return "approval/approval-list";
    }

    @GetMapping("/approvals/{requestId}")
    public String approvalDetails(
            @PathVariable Long requestId,
            Model model) {

        // Temporary data.
        // Later this will load the actual ApprovalRequest
        // and ShareholderChangeRequest from Oracle.

        model.addAttribute("requestId", requestId);

        return "approval/approval-details";
    }
}