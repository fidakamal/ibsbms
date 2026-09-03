package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.service.ShareholderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ShareholderController {

    private final ShareholderService shareholderService;

    public ShareholderController(ShareholderService shareholderService) {
        this.shareholderService = shareholderService;
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

        String proposalJson =
                shareholderService.buildCreateProposalJson(request);

        System.out.println("Shareholder Create Proposal:");
        System.out.println(proposalJson);

        return "redirect:/shareholders";
    }
}