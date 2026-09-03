package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository
        extends JpaRepository<ApprovalRequest, Long> {
}