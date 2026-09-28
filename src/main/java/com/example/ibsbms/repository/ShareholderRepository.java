package com.example.ibsbms.repository;

import com.example.ibsbms.entity.Shareholder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShareholderRepository
        extends JpaRepository<Shareholder, String> {

    Optional<Shareholder> findByFolioBoAndIsValid(
            String folioBo,
            Integer isValid
    );

    Optional<Shareholder> findByFolioBo(
            String folioBo
    );

    @Query(value = """
    SELECT *
    FROM (
        SELECT
            s.FOLIO_BO AS "folioBo",
            s.CUST_NAME AS "custName",
            s.EMAIL AS "email",
            s.PHONE AS "phone",
            s.SHARES AS "shares",
            s.BALANCE AS "balance",
            s.IS_VALID AS "isValid",

            a.ADD1 AS "add1",
            a.ADD2 AS "add2",
            a.ADD3 AS "add3",
            a.ADD4 AS "add4",
            a.COUNTRY_NAME AS "countryName",

            ROW_NUMBER() OVER (
                ORDER BY s.REGISTRATION_DATE DESC NULLS LAST,
                         s.FOLIO_BO DESC
            ) AS rn

        FROM T_ACCOUNT_SHARE s

        LEFT JOIN T_ADDRESS_SHARE a
            ON s.FOLIO_BO = a.FOLIO_BO

        WHERE (
            :search IS NULL
            OR TRIM(:search) = ''
            OR s.FOLIO_BO = :search
            OR EXISTS (
                SELECT 1
                FROM T_SHARE_MOVEMENT m
                WHERE m.SOURCE_REF = s.FOLIO_BO
                  AND m.MOVEMENT_TYPE = 'DEMAT'
                  AND m.SOURCE_TYPE = 'FOLIO'
                  AND m.TARGET_TYPE = 'BO'
                  AND m.TARGET_REF = :search
            )
        )
    )
    WHERE rn BETWEEN :startRow AND :endRow
    """,
            nativeQuery = true)
    List<ShareholderListProjection> findShareholderList(
            @Param("search") String search,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow
    );

    @Query(value = """
    SELECT COUNT(DISTINCT s.FOLIO_BO)
    FROM T_ACCOUNT_SHARE s
    WHERE (
        :search IS NULL
        OR TRIM(:search) = ''
        OR s.FOLIO_BO = :search
        OR EXISTS (
            SELECT 1
            FROM T_SHARE_MOVEMENT m
            WHERE m.SOURCE_REF = s.FOLIO_BO
              AND m.MOVEMENT_TYPE = 'DEMAT'
              AND m.SOURCE_TYPE = 'FOLIO'
              AND m.TARGET_TYPE = 'BO'
              AND m.TARGET_REF = :search
        )
    )
    """,
            nativeQuery = true)
    long countShareholders(
            @Param("search") String search
    );



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT s
    FROM Shareholder s
    WHERE s.folioBo IN :folioBos
    ORDER BY s.oid
""")
    List<Shareholder> findAllByFolioBoInForUpdate(
            @Param("folioBos") List<String> folioBos
    );



    @Query(value = """
    SELECT DISTINCT s.FOLIO_BO AS "folioBo"
    FROM T_ACCOUNT_SHARE s
    JOIN T_SHARE_MOVEMENT m
        ON m.SOURCE_REF = s.FOLIO_BO
    WHERE m.MOVEMENT_TYPE = 'DEMAT'
      AND m.SOURCE_TYPE = 'FOLIO'
      AND m.TARGET_TYPE = 'BO'
      AND m.TARGET_REF = :boNo
    """,
            nativeQuery = true)
    List<String> findFoliosByBo(
            @Param("boNo") String boNo
    );
}

