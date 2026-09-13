package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository
        extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByBusinessRefAndStatusIn(
            String businessRef,
            Collection<String> statuses
    );

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

    @Query("""
    SELECT a
    FROM ApprovalRequest a
    WHERE a.status = 'REJECTED'
      AND a.sourceType = 'SHAREHOLDER_CHANGE'
      AND a.operationCode IN ('SHAREHOLDER_CREATE', 'SHAREHOLDER_UPDATE')
    ORDER BY a.decidedAt DESC
""")
    List<ApprovalRequest> findRejectedRequests();

    @Query("""
    SELECT a
    FROM ApprovalRequest a
    WHERE a.status = 'RETURNED_FOR_MODIFICATION'
      AND a.currentStage = 'MAKER'
      AND a.makerId = :makerId
      AND a.operationCode IN ('SHAREHOLDER_CREATE', 'SHAREHOLDER_UPDATE')
    ORDER BY a.updatedAt DESC
""")
    List<ApprovalRequest> findReturnedForModificationRequests(
            @Param("makerId") String makerId
    );


    @Query("""
    SELECT a
    FROM ApprovalRequest a
    WHERE a.makerId = :makerId
      AND a.sourceType = 'SHAREHOLDER_CHANGE'
      AND a.operationCode = 'SHAREHOLDER_CREATE'
    ORDER BY a.createdAt DESC
""")
    List<ApprovalRequest> findMakerCreateRequests(
            @Param("makerId") String makerId
    );


    @Query("""
    SELECT a
    FROM ApprovalRequest a
    WHERE a.status = 'RETURNED_FOR_MODIFICATION'
      AND a.currentStage = 'MAKER'
      AND a.sourceType = 'SHAREHOLDER_CHANGE'
      AND a.operationCode IN ('SHAREHOLDER_CREATE', 'SHAREHOLDER_UPDATE')
    ORDER BY a.updatedAt DESC
""")
    List<ApprovalRequest> findReturnedForModificationRequests();
}