package com.example.ibsbms.dto;

import com.example.ibsbms.enums.TransferAuthStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShareTransferCheckerDetail(
        String trId,
        String transferType,
        LocalDate businessDate,
        String source,
        String destination,
        BigDecimal quantity,
        String instrumentNo,
        LocalDate instrumentDate,
        String particulars,
        String remarks,
        String makerId,
        String makerIp,
        LocalDateTime submittedAt,
        TransferAuthStatus status
) {
}