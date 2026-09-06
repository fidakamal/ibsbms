package com.example.ibsbms.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class WorkflowIdService {

    @PersistenceContext
    private EntityManager entityManager;

    public String generateChangeId() {
        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_CHANGE_ID_OID.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.toString();
    }

    public Long nextApprovalRequestId() {
        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_APPROVAL_REQUEST.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.longValue();
    }

    public Long nextApprovalActionId() {
        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_APPROVAL_ACTION.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.longValue();
    }

    public Long nextBusinessAuditId() {
        Number value = (Number) entityManager
                .createNativeQuery(
                        "SELECT SEQ_BUSINESS_AUDIT.NEXTVAL FROM DUAL"
                )
                .getSingleResult();

        return value.longValue();
    }


    public String generateNextFolioBo() {

        Number value = (Number) entityManager
                .createNativeQuery("""
                SELECT NVL(
                    MAX(
                        CASE
                            WHEN REGEXP_LIKE(TRIM(FOLIO_BO), '^[0-9]+$')
                            THEN TO_NUMBER(TRIM(FOLIO_BO))
                        END
                    ),
                    0
                ) + 1
                FROM T_ACCOUNT_SHARE
                """)
                .getSingleResult();

        return value.toString();
    }
}