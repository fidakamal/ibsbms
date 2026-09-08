package com.example.ibsbms.repository;

import com.example.ibsbms.entity.Shareholder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShareholderRepository
        extends JpaRepository<Shareholder, String> {

    Optional<Shareholder> findByFolioBoAndIsValid(
            String folioBo,
            Integer isValid
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
                    ORDER BY s.FOLIO_BO
                ) AS rn

            FROM T_ACCOUNT_SHARE s

            LEFT JOIN T_ADDRESS_SHARE a
                ON s.FOLIO_BO = a.FOLIO_BO

            WHERE (
                :folioBo IS NULL
                OR TRIM(:folioBo) = ''
                OR s.FOLIO_BO = :folioBo
            )
        )
        WHERE rn BETWEEN :startRow AND :endRow
        """,
            nativeQuery = true)
    List<ShareholderListProjection> findShareholderList(
            @Param("folioBo") String folioBo,
            @Param("startRow") int startRow,
            @Param("endRow") int endRow
    );

    @Query(value = """
        SELECT COUNT(DISTINCT s.FOLIO_BO)
        FROM T_ACCOUNT_SHARE s
        WHERE (
            :folioBo IS NULL
            OR TRIM(:folioBo) = ''
            OR s.FOLIO_BO = :folioBo
        )
        """,
            nativeQuery = true)
    long countShareholders(
            @Param("folioBo") String folioBo
    );
}