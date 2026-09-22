package com.example.ibsbms.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class BusinessDateService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Dhaka");

    public LocalDate currentBusinessDate() {
        return LocalDate.now(BUSINESS_ZONE);
    }

    public boolean isCurrentBusinessDate(LocalDate date) {
        return currentBusinessDate().equals(date);
    }
}