package com.example.ibsbms.repository;

import com.example.ibsbms.entity.TransAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransAuthRepository extends JpaRepository<TransAuth, String> {

    /*
     * ShareTransferWorkflowService.submitForApproval() always inserts
     * TWO T_TRANS_AUTH rows per transfer request sharing the same
     * TR_ID: a debit leg (DR_AMT = quantity, CR_AMT = 0) and a credit
     * leg (DR_AMT = 0, CR_AMT = quantity).
     *
     * For a "My Pending / Returned" list we want ONE row per transfer
     * request, so we only read the debit leg (DR_AMT > 0); the credit
     * side is read back from CONTRA_ACC_NO.
     *
     * NOTE: deliberately returns a List, not a Spring Data Page/
     * Pageable. The target Oracle instance does not support the
     * "FETCH FIRST ? ROWS ONLY" pagination syntax Hibernate generates
     * for Pageable queries (ORA-00933) - the same reason
     * ApprovalWorkflowService.searchMakerRequests() fetches everything
     * and paginates in Java. Do the same here.
     */
    @Query("""
        SELECT t
        FROM TransAuth t
        WHERE t.makerId = :makerId
          AND t.drAmt IS NOT NULL
          AND t.drAmt > 0
        ORDER BY t.modifyDate DESC, t.trId DESC
    """)
    List<TransAuth> findMakerDebitLegs(
            @Param("makerId") String makerId
    );

    @Query("""
        SELECT t
        FROM TransAuth t
        WHERE t.makerId = :makerId
          AND t.drAmt IS NOT NULL
          AND t.drAmt > 0
          AND t.trState = :trState
        ORDER BY t.modifyDate DESC, t.trId DESC
    """)
    List<TransAuth> findMakerDebitLegsByState(
            @Param("makerId") String makerId,
            @Param("trState") Integer trState
    );
}