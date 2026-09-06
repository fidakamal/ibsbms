package com.example.ibsbms.controller;

import com.example.ibsbms.entity.Shareholder;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ShareholderRepository shareholderRepository;

    public HomeController(ShareholderRepository shareholderRepository) {
        this.shareholderRepository = shareholderRepository;
    }

    @GetMapping("/")
    public String home() {
        return "dashboard";
    }

    @GetMapping("/shareholders")
    public String shareholders(Model model) {

        List<Shareholder> shareholders =
                shareholderRepository.findAll();

        model.addAttribute("shareholders", shareholders);

        return "shareholder/shareholder-list";
    }
}