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

    @Query("""
        SELECT t
        FROM TransAuth t
        WHERE t.trState IN (0, -1)
          AND t.drAmt IS NOT NULL
          AND t.drAmt > 0
        ORDER BY t.modifyDate DESC, t.trId DESC
    """)
    List<TransAuth> findPendingCheckerDebitLegs();

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