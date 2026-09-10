package com.example.ibsbms.service;

import com.example.ibsbms.repository.DashboardRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(
            DashboardRepository dashboardRepository) {

        this.dashboardRepository = dashboardRepository;
    }


    public long getTotalShareholders() {

        return dashboardRepository.countTotalShareholders();
    }


    public long getActiveAccounts() {

        return dashboardRepository.countActiveAccounts();
    }


    public long getPendingApprovals() {

        return dashboardRepository.countPendingApprovals();
    }


    public long getDormantAccounts() {

        return dashboardRepository.countDormantAccounts();
    }
}

