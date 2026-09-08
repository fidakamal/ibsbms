package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalActionRepository
        extends JpaRepository<ApprovalAction, Long> {

    List<ApprovalAction> findByRequestIdOrderByActionAtAsc(
            Long requestId
    );

    List<ApprovalAction> findByRequestIdAndActionOrderByActionAtDesc(
            Long requestId,
            String action
    );
}