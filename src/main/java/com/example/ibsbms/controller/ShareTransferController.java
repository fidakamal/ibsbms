package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ReturnedTransferEditView;
import com.example.ibsbms.dto.ShareTransferForm;
import com.example.ibsbms.dto.ShareTransferRequestSummary;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

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

    @GetMapping("/share-transfer/my-requests")
    public String myRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {

        if (size != 10 && size != 25 && size != 50 && size != 100) {
            size = 10;
        }

        if (page < 1) {
            page = 1;
        }

        String makerId = principal.getName();
        String statusFilter = (status == null || status.isBlank()) ? "ALL" : status.toUpperCase();

        List<ShareTransferRequestSummary> allRequests =
                shareTransferWorkflowService.getMyRequests(makerId, statusFilter);

        int totalItems = allRequests.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalItems);

        List<ShareTransferRequestSummary> requests =
                totalItems == 0
                        ? new java.util.ArrayList<>()
                        : allRequests.subList(startIndex, endIndex);

        long startEntry = totalItems == 0 ? 0 : startIndex + 1;
        long endEntry = endIndex;

        model.addAttribute("requests", requests);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startEntry", startEntry);
        model.addAttribute("endEntry", endEntry);

        return "share-transfer/my-requests";
    }

    @GetMapping("/share-transfer/returned/{trId}/edit")
    public String editReturned(
            @PathVariable String trId,
            Model model,
            Principal principal) {

        String makerId = principal.getName();

        ReturnedTransferEditView view =
                shareTransferWorkflowService.getReturnedRequestForEdit(trId, makerId);

        model.addAttribute("shareTransferForm", view.getForm());
        model.addAttribute("transferTypes", TransferType.values());
        model.addAttribute("businessDate", businessDateService.currentBusinessDate());
        model.addAttribute("editMode", true);
        model.addAttribute("trId", view.getTrId());
        model.addAttribute("returnRemarks", view.getReturnRemarks());

        return "share-transfer/share-transfer-form";
    }

    @PostMapping("/share-transfer/returned/{trId}/resubmit")
    public String resubmitReturned(
            @PathVariable String trId,
            @Valid @ModelAttribute("shareTransferForm") ShareTransferForm form,
            BindingResult bindingResult,
            Model model,
            Principal principal) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("transferTypes", TransferType.values());
            model.addAttribute("businessDate", businessDateService.currentBusinessDate());
            model.addAttribute("editMode", true);
            model.addAttribute("trId", trId);
            return "share-transfer/share-transfer-form";
        }

        String makerId = principal.getName();
        String makerIp = "127.0.0.1";

        try {

            shareTransferWorkflowService.resubmitReturned(trId, form, makerId, makerIp);

            return "redirect:/share-transfer/my-requests?success=Transfer request "
                    + trId + " resubmitted for checker approval.";

        } catch (ShareTransferValidationException | AccountNotFoundException e) {

            model.addAttribute("shareTransferForm", form);
            model.addAttribute("transferTypes", TransferType.values());
            model.addAttribute("businessDate", businessDateService.currentBusinessDate());
            model.addAttribute("editMode", true);
            model.addAttribute("trId", trId);
            model.addAttribute("submitError", e.getMessage());

            return "share-transfer/share-transfer-form";
        }
    }
}