```java
package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.entity.ApprovalRequest;
import com.example.ibsbms.service.ShareholderService;
import com.example.ibsbms.service.ShareholderWorkflowService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ShareholderController {

    private final ShareholderWorkflowService shareholderWorkflowService;
    private final ShareholderService shareholderService;

    public ShareholderController(
            ShareholderWorkflowService shareholderWorkflowService,
            ShareholderService shareholderService) {

        this.shareholderWorkflowService = shareholderWorkflowService;
        this.shareholderService = shareholderService;
    }

    /*
     * ==========================================================
     * Create
     * ==========================================================
     */
    @GetMapping("/shareholders/create")
    public String createForm(Model model) {

        model.addAttribute(
                "shareholder",
                new ShareholderCreateRequest()
        );

        model.addAttribute(
                "formAction",
                "/shareholders/create"
        );

        model.addAttribute(
                "editMode",
                false
        );

        model.addAttribute(
                "folioBo",
                null
        );

        return "shareholder/shareholder-create";
    }

    @PostMapping("/shareholders/create")
    public String createShareholder(
            @Valid @ModelAttribute("shareholder")
            ShareholderCreateRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "formAction",
                    "/shareholders/create"
            );

            model.addAttribute(
                    "editMode",
                    false
            );

            model.addAttribute(
                    "folioBo",
                    null
            );

            return "shareholder/shareholder-create";
        }

        /*
         * Temporary maker identity.
         *
         * Later this will come from the authenticated session,
         * never from the browser.
         */
        String makerId = "test.maker";

        /*
         * Temporary IP.
         *
         * Later this will come from the HTTP request/gateway.
         */
        String makerIp = "127.0.0.1";

        shareholderWorkflowService.submitCreateForApproval(
                request,
                makerId,
                makerIp
        );

        return "redirect:/shareholders";
    }

    /*
     * ==========================================================
     * Modify (Edit)
     *
     * Only an already-approved shareholder can be opened this
     * way. The actual modification is submitted as a
     * SHAREHOLDER_UPDATE approval request.
     * ==========================================================
     */
    @GetMapping("/shareholders/{folioBo}/edit")
    public String editForm(
            @PathVariable String folioBo,
            Model model) {

        ShareholderCreateRequest snapshot;

        try {

            snapshot =
                    shareholderService.snapshotOf(folioBo);

        } catch (IllegalArgumentException e) {

            return "redirect:/shareholders?error="
                    + e.getMessage();
        }

        model.addAttribute(
                "shareholder",
                snapshot
        );

        model.addAttribute(
                "formAction",
                "/shareholders/"
                        + folioBo
                        + "/edit"
        );

        model.addAttribute(
                "editMode",
                true
        );

        model.addAttribute(
                "folioBo",
                folioBo
        );

        return "shareholder/shareholder-create";
    }

    @PostMapping("/shareholders/{folioBo}/edit")
    public String submitEdit(
            @PathVariable String folioBo,
            @Valid @ModelAttribute("shareholder")
            ShareholderCreateRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "formAction",
                    "/shareholders/"
                            + folioBo
                            + "/edit"
            );

            model.addAttribute(
                    "editMode",
                    true
            );

            model.addAttribute(
                    "folioBo",
                    folioBo
            );

            return "shareholder/shareholder-create";
        }

        /*
         * Temporary maker identity.
         *
         * Later this will come from authentication.
         */
        String makerId = "test.maker";
        String makerIp = "127.0.0.1";

        try {

            shareholderWorkflowService.submitModifyForApproval(
                    folioBo,
                    request,
                    makerId,
                    makerIp
            );

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            return "redirect:/shareholders/"
                    + folioBo
                    + "/edit?error="
                    + e.getMessage();
        }

        return "redirect:/shareholders?success="
                + "Modification submitted for approval.";
    }

    /*
     * ==========================================================
     * Returned for Modification
     * ==========================================================
     */

    @GetMapping("/shareholders/returned")
    public String returnedRequests(Model model) {

        String makerId = "test.maker";

        var requests =
                shareholderWorkflowService
                        .getReturnedForModificationRequests(
                                makerId
                        );

        java.util.Map<Long, String> returnRemarks =
                new java.util.HashMap<>();

        for (ApprovalRequest request : requests) {

            returnRemarks.put(
                    request.getRequestId(),
                    shareholderWorkflowService
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

        return "shareholder/returned-list";
    }

    /*
     * ==========================================================
     * Edit Returned Request
     * ==========================================================
     */
    @GetMapping(
            "/shareholders/returned/{requestId}/edit"
    )
    public String editReturnedRequest(
            @PathVariable Long requestId,
            Model model) {

        String makerId = "test.maker";

        try {

            ShareholderCreateRequest snapshot =
                    shareholderWorkflowService
                            .getReturnedRequestData(
                                    requestId,
                                    makerId
                            );

            model.addAttribute(
                    "shareholder",
                    snapshot
            );

            model.addAttribute(
                    "formAction",
                    "/shareholders/returned/"
                            + requestId
                            + "/resubmit"
            );

            model.addAttribute(
                    "editMode",
                    true
            );

            model.addAttribute(
                    "returnedRequestId",
                    requestId
            );

            return "shareholder/shareholder-create";

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            return "redirect:/shareholders/returned?error="
                    + e.getMessage();
        }
    }

    /*
     * ==========================================================
     * Resubmit Returned Request
     * ==========================================================
     */
    @PostMapping(
            "/shareholders/returned/{requestId}/resubmit"
    )
    public String resubmitReturned(
            @PathVariable Long requestId,
            @Valid @ModelAttribute("shareholder")
            ShareholderCreateRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "formAction",
                    "/shareholders/returned/"
                            + requestId
                            + "/resubmit"
            );

            model.addAttribute(
                    "editMode",
                    true
            );

            model.addAttribute(
                    "returnedRequestId",
                    requestId
            );

            return "shareholder/shareholder-create";
        }

        /*
         * Temporary maker identity.
         *
         * Later this will come from authentication.
         */
        String makerId = "test.maker";
        String makerIp = "127.0.0.1";

        try {

            shareholderWorkflowService.resubmitReturnedModify(
                    requestId,
                    request,
                    makerId,
                    makerIp
            );

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            return "redirect:/shareholders/returned?error="
                    + e.getMessage();
        }

        return "redirect:/shareholders/returned?success="
                + "Request resubmitted successfully.";
    }
}
```
