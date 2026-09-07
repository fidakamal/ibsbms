package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ApprovalRequestRepository
        extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByBusinessRefAndStatusIn(
            String businessRef,
            Collection<String> statuses
    );
}