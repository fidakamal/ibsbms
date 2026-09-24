package com.example.ibsbms.repository;

import com.example.ibsbms.entity.TransAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}