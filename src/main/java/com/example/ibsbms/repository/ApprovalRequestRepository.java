package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository
        extends JpaRepository<ApprovalRequest, Long> {

    @Query("""
    SELECT a
    FROM ApprovalRequest a
    WHERE a.status = 'PENDING_CHECKER'
      AND a.currentStage = 'CHECKER'
      AND a.sourceType = 'SHAREHOLDER_CHANGE'
      AND a.operationCode IN ('SHAREHOLDER_CREATE', 'SHAREHOLDER_UPDATE')
    ORDER BY a.createdAt DESC
""")
    List<ApprovalRequest> findPendingCheckerRequests();

    Optional<ApprovalRequest> findByRequestId(Long requestId);
}