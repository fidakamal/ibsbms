package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareTransferForm;
import com.example.ibsbms.enums.TransferType;
import com.example.ibsbms.exception.AccountNotFoundException;
import com.example.ibsbms.exception.ShareTransferValidationException;
import com.example.ibsbms.service.BusinessDateService;
import com.example.ibsbms.service.ShareTransferWorkflowService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class ShareTransferController {

    private final BusinessDateService businessDateService;
    private final ShareTransferWorkflowService shareTransferWorkflowService;

    public ShareTransferController(
            BusinessDateService businessDateService,
            ShareTransferWorkflowService shareTransferWorkflowService) {

        this.businessDateService = businessDateService;
        this.shareTransferWorkflowService = shareTransferWorkflowService;
    }

    @GetMapping("/share-transfer")
    public String showForm(Model model) {

        if (!model.containsAttribute("shareTransferForm")) {
            model.addAttribute("shareTransferForm", new ShareTransferForm());
        }

        model.addAttribute("transferTypes", TransferType.values());
        model.addAttribute("businessDate", businessDateService.currentBusinessDate());

        return "share-transfer/share-transfer-form";
    }

    @PostMapping("/share-transfer")
    public String submit(
            @Valid @ModelAttribute("shareTransferForm") ShareTransferForm form,
            BindingResult bindingResult,
            Model model,
            Principal principal) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("transferTypes", TransferType.values());
            model.addAttribute("businessDate", businessDateService.currentBusinessDate());
            return "share-transfer/share-transfer-form";
        }

        String makerId = principal.getName();
        String makerIp = "127.0.0.1";

        try {

            String trId = shareTransferWorkflowService.submitForApproval(form, makerId, makerIp);

            return "redirect:/share-transfer?success=Transfer request "
                    + trId
                    + " submitted for checker approval.";

        } catch (ShareTransferValidationException | AccountNotFoundException e) {

            model.addAttribute("shareTransferForm", form);
            model.addAttribute("transferTypes", TransferType.values());
            model.addAttribute("businessDate", businessDateService.currentBusinessDate());
            model.addAttribute("submitError", e.getMessage());

            return "share-transfer/share-transfer-form";
        }
    }
}