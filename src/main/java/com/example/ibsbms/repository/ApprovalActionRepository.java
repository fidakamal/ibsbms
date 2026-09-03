package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalActionRepository
        extends JpaRepository<ApprovalAction, Long> {
}