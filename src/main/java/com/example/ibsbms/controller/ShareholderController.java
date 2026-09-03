package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.service.ShareholderService;
import com.example.ibsbms.service.ShareholderWorkflowService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ShareholderController {

    private final ShareholderWorkflowService shareholderWorkflowService;

    public ShareholderController(
            ShareholderWorkflowService shareholderWorkflowService) {

        this.shareholderWorkflowService = shareholderWorkflowService;
    }

    @GetMapping("/shareholders/create")
    public String createForm() {
        return "shareholder/shareholder-create";
    }

    @PostMapping("/shareholders/create")
    public String createShareholder(
            @Valid @ModelAttribute("shareholder")
            ShareholderCreateRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
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
}