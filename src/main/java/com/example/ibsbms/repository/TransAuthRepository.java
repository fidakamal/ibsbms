package com.example.ibsbms.repository;

import com.example.ibsbms.entity.TransAuth;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransAuthRepository extends JpaRepository<TransAuth, String> {

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

    List<TransAuth> findByTrId(String trId);

    /**
     * Returns all authorization legs that are currently pending.
     *
     * Each transfer has two rows in T_TRANS_AUTH:
     *   - debit leg
     *   - credit leg
     *
     * We only need the debit leg for the checker queue because it
     * contains the source account and the contra account.
     */
    @Query("""
        SELECT t
        FROM TransAuth t
        WHERE t.trState = 0
          AND t.drAmt IS NOT NULL
          AND t.drAmt > 0
        ORDER BY t.modifyDate DESC, t.trId DESC
    """)
    List<TransAuth> findPendingCheckerDebitLegs();

    /**
     * Locks the debit leg while checker approval is being processed.
     *
     * This is the first layer of double-approval protection.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TransAuth t
        WHERE t.trId = :trId
          AND t.drAmt IS NOT NULL
          AND t.drAmt > 0
    """)
    Optional<TransAuth> findPendingDebitForUpdate(
            @Param("trId") String trId
    );

    /**
     * Locks the credit leg belonging to the same transfer.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TransAuth t
        WHERE t.trId = :trId
          AND t.crAmt IS NOT NULL
          AND t.crAmt > 0
    """)
    Optional<TransAuth> findCreditForUpdate(
            @Param("trId") String trId
    );
}