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


    @GetMapping("/approvals/returned")
    public String returnedRequests(Model model) {

        List<ApprovalRequest> requests =
                approvalWorkflowService
                        .getReturnedForModificationRequests();

        java.util.Map<Long, String> returnRemarks =
                new java.util.HashMap<>();

        for (ApprovalRequest request : requests) {

            returnRemarks.put(
                    request.getRequestId(),
                    approvalWorkflowService
                            .getLatestReturnRemarks(
                                    request.getRequestId()
                            )
            );
        }

        model.addAttribute(
                "returnedRequests",
                requests
        );

        model.addAttribute(
                "returnedCount",
                requests.size()
        );

        model.addAttribute(
                "returnRemarks",
                returnRemarks
        );

        return "approval/returned-list";
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

            return "redirect:/shareholders?success=Request approved successfully.";

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

            if (remarks == null || remarks.trim().isEmpty()) {

                return "redirect:/approvals/"
                        + requestId
                        + "?error=Remarks are required. Please provide a reason before returning this request for modification.";
            }

            String checkerId = principal.getName();
            String checkerIp = "127.0.0.1";

            approvalWorkflowService.returnForModification(
                    requestId,
                    checkerId,
                    checkerIp,
                    remarks
            );

            return "redirect:/approvals/returned?success=Request returned to maker for modification.";

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

            if (remarks == null || remarks.trim().isEmpty()) {

                return "redirect:/approvals/"
                        + requestId
                        + "?error=Remarks are required. Please provide a reason before rejecting this request.";
            }

            String checkerId = principal.getName();
            String checkerIp = "127.0.0.1";

            approvalWorkflowService.reject(
                    requestId,
                    checkerId,
                    checkerIp,
                    remarks
            );

            return "redirect:/approvals/rejected?success=Request rejected successfully.";

        } catch (Exception e) {

            return "redirect:/approvals/"
                    + requestId
                    + "?error="
                    + e.getMessage();
        }
    }


    @GetMapping("/my-requests")
    public String myRequests(
            @RequestParam(required = false) String customerName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {

        String makerId = principal.getName();

        /*
         * Allow only the page sizes we want.
         */
        if (size != 10 &&
                size != 25 &&
                size != 50 &&
                size != 100) {

            size = 10;
        }

        if (page < 1) {
            page = 1;
        }

        /*
         * If there is a customer-name search, we currently
         * filter the requests in memory.
         *
         * Therefore pagination should be applied after the
         * search filtering.
         */
        List<ApprovalRequest> allRequests =
                approvalWorkflowService.searchMakerRequests(
                        makerId,
                        customerName
                );

        int totalItems = allRequests.size();

        int totalPages =
                (int) Math.ceil(
                        (double) totalItems / size
                );

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        int startIndex =
                (page - 1) * size;

        int endIndex =
                Math.min(
                        startIndex + size,
                        totalItems
                );

        List<ApprovalRequest> requests;

        if (totalItems == 0) {

            requests = new java.util.ArrayList<>();

        } else {

            requests =
                    allRequests.subList(
                            startIndex,
                            endIndex
                    );
        }

        long startEntry = 0;
        long endEntry = 0;

        if (totalItems > 0) {

            startEntry =
                    startIndex + 1;

            endEntry =
                    endIndex;
        }

        model.addAttribute(
                "requests",
                requests
        );

        model.addAttribute(
                "customerName",
                customerName
        );

        model.addAttribute(
                "page",
                page
        );

        model.addAttribute(
                "pageSize",
                size
        );

        model.addAttribute(
                "totalItems",
                totalItems
        );

        model.addAttribute(
                "totalPages",
                totalPages
        );

        model.addAttribute(
                "startEntry",
                startEntry
        );

        model.addAttribute(
                "endEntry",
                endEntry
        );

        return "approval/my-requests";
    }


    @GetMapping("/my-requests/{requestId}")
    public String myRequestDetails(
            @PathVariable Long requestId,
            Principal principal,
            Model model) {

        String makerId = principal.getName();

        ApprovalRequest approvalRequest =
                approvalWorkflowService
                        .getApprovalRequest(requestId);

        // Security check:
        // A maker can only view their own requests.
        if (!makerId.equals(approvalRequest.getMakerId())) {
            return "redirect:/my-requests?error=You are not authorized to view this request.";
        }

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

        return "approval/my-request-details";
    }
}