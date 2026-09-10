package com.example.ibsbms.controller;

import com.example.ibsbms.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;


    public DashboardController(
            DashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "totalShareholders",
                dashboardService.getTotalShareholders()
        );

        model.addAttribute(
                "activeAccounts",
                dashboardService.getActiveAccounts()
        );

        model.addAttribute(
                "pendingApprovals",
                dashboardService.getPendingApprovals()
        );

        model.addAttribute(
                "dormantAccounts",
                dashboardService.getDormantAccounts()
        );

        return "dashboard";
    }
}

