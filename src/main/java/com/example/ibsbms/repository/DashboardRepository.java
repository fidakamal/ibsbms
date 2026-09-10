package com.example.ibsbms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository
        extends JpaRepository<com.example.ibsbms.entity.Shareholder, String> {

    @Query(value = """
        SELECT COUNT(*)
        FROM T_ACCOUNT_SHARE
        """,
            nativeQuery = true)
    long countTotalShareholders();


    @Query(value = """
        SELECT COUNT(*)
        FROM T_ACCOUNT_SHARE
        WHERE IS_VALID = 1
        """,
            nativeQuery = true)
    long countActiveAccounts();


    @Query(value = """
        SELECT COUNT(*)
        FROM T_APPROVAL_REQUEST
        WHERE STATUS = 'PENDING_CHECKER'
          AND CURRENT_STAGE = 'CHECKER'
          AND SOURCE_TYPE = 'SHAREHOLDER_CHANGE'
          AND OPERATION_CODE IN (
              'SHAREHOLDER_CREATE',
              'SHAREHOLDER_UPDATE'
          )
        """,
            nativeQuery = true)
    long countPendingApprovals();


    @Query(value = """
        SELECT COUNT(*)
        FROM T_ACCOUNT_SHARE
        WHERE IS_VALID = 0
        """,
            nativeQuery = true)
    long countDormantAccounts();
}

