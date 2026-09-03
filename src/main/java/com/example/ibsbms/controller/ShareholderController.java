package com.example.ibsbms.controller;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ShareholderController {

    @GetMapping("/shareholders/create")
    public String createForm() {
        return "shareholder/shareholder-create";
    }

    @PostMapping("/shareholders/create")
    public String createShareholder(
            @Valid @ModelAttribute("shareholder") ShareholderCreateRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "shareholder/shareholder-create";
        }

        // Service will be added next.
        return "redirect:/shareholders";
    }
}