package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareTransferForm;
import com.example.ibsbms.enums.TransferType;
import com.example.ibsbms.service.BusinessDateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShareTransferController {

    private final BusinessDateService businessDateService;

    public ShareTransferController(BusinessDateService businessDateService) {
        this.businessDateService = businessDateService;
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
}