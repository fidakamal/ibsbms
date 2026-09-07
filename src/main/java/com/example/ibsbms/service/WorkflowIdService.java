package com.example.ibsbms.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class WorkflowIdService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Generates a unique Change ID using the existing Oracle sequence.
     */
    public String generateChangeId() {

        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_CHANGE_ID_OID.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.toString();
    }

    /**
     * Generates the next Approval Request ID.
     */
    public Long nextApprovalRequestId() {

        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_APPROVAL_REQUEST.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.longValue();
    }

    /**
     * Generates the next Approval Action ID.
     */
    public Long nextApprovalActionId() {

        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_APPROVAL_ACTION.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.longValue();
    }

    /**
     * Generates the next Business Audit ID.
     */
    public Long nextBusinessAuditId() {

        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_BUSINESS_AUDIT.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.longValue();
    }

    /**
     * Generates the next physical Folio BO.
     *
     * The existing system uses MAX + 1 for numeric Folio BO values.
     *
     * We must consider BOTH:
     *
     * 1. T_ACCOUNT_SHARE
     *    - Folios that have already been approved and inserted
     *
     * 2. T_SHAREHOLDER_CHANGE_REQUEST
     *    - Folios that have already been reserved by pending Create requests
     *
     * This prevents a pending request from reserving a Folio that is
     * generated again before the first request is approved.
     */
    public String generateNextFolioBo() {

        Number value = (Number) entityManager
                .createNativeQuery("""
                    SELECT GREATEST(
                        NVL(
                            (
                                SELECT MAX(
                                    CASE
                                        WHEN REGEXP_LIKE(TRIM(FOLIO_BO), '^[0-9]+$')
                                        THEN TO_NUMBER(TRIM(FOLIO_BO))
                                    END
                                )
                                FROM T_ACCOUNT_SHARE
                            ),
                            0
                        ),
                        NVL(
                            (
                                SELECT MAX(
                                    CASE
                                        WHEN REGEXP_LIKE(TRIM(FOLIO_BO), '^[0-9]+$')
                                        THEN TO_NUMBER(TRIM(FOLIO_BO))
                                    END
                                )
                                FROM T_SHAREHOLDER_CHANGE_REQUEST
                                WHERE OPERATION_CODE = 'SHAREHOLDER_CREATE'
                            ),
                            0
                        )
                    ) + 1
                    FROM DUAL
                    """)
                .getSingleResult();

        return value.toString();
    }

    /**
     * Generates the next OID for T_ACCOUNT_SHARE.
     *
     * The current implementation follows the supervisor-confirmed
     * MAX + 1 approach.
     */
    public String generateNextAccountShareOid() {

        Number value = (Number) entityManager
                .createNativeQuery("""
                    SELECT NVL(
                        MAX(
                            CASE
                                WHEN REGEXP_LIKE(TRIM(OID), '^[0-9]+$')
                                THEN TO_NUMBER(TRIM(OID))
                            END
                        ),
                        0
                    ) + 1
                    FROM T_ACCOUNT_SHARE
                    """)
                .getSingleResult();

        return value.toString();
    }

    /**
     * Generates the next OID for T_ADDRESS_SHARE.
     */
    public String generateNextAddressShareOid() {

        Number value = (Number) entityManager
                .createNativeQuery("""
                    SELECT NVL(
                        MAX(
                            CASE
                                WHEN REGEXP_LIKE(TRIM(OID), '^[0-9]+$')
                                THEN TO_NUMBER(TRIM(OID))
                            END
                        ),
                        0
                    ) + 1
                    FROM T_ADDRESS_SHARE
                    """)
                .getSingleResult();

        return value.toString();
    }

    /**
     * Generates the next OID for T_BANKINFO_SHARE.
     */
    public String generateNextBankInfoShareOid() {

        Number value = (Number) entityManager
                .createNativeQuery("""
                    SELECT NVL(
                        MAX(
                            CASE
                                WHEN REGEXP_LIKE(TRIM(OID), '^[0-9]+$')
                                THEN TO_NUMBER(TRIM(OID))
                            END
                        ),
                        0
                    ) + 1
                    FROM T_BANKINFO_SHARE
                    """)
                .getSingleResult();

        return value.toString();
    }
}


