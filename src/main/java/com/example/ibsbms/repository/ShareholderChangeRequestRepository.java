package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ShareholderChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShareholderChangeRequestRepository
        extends JpaRepository<ShareholderChangeRequest, String> {

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM ShareholderChangeRequest s
        WHERE s.folioBo = :folioBo
          AND s.operationCode = :operationCode
    """)
    boolean existsByFolioBoAndOperationCode(
            @Param("folioBo") String folioBo,
            @Param("operationCode") String operationCode
    );

    Optional<ShareholderChangeRequest> findByChangeId(String changeId);
}