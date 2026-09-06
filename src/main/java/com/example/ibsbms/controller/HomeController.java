package com.example.ibsbms.controller;

import com.example.ibsbms.repository.ShareholderListProjection;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String shareholders(
            @RequestParam(required = false) String folioBo,
            Model model) {

        String searchFolio = folioBo;

        if (searchFolio != null) {
            searchFolio = searchFolio.trim();

            if (searchFolio.isEmpty()) {
                searchFolio = null;
            }
        }

        List<ShareholderListProjection> shareholders =
                shareholderRepository.findShareholderList(searchFolio);

        model.addAttribute("shareholders", shareholders);
        model.addAttribute("folioBo", folioBo);

        return "shareholder/shareholder-list";
    }
}