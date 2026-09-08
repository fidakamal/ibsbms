package com.example.ibsbms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_BUSINESS_AUDIT")
public class BusinessAudit {

    @Id
    @Column(name = "AUDIT_ID", nullable = false)
    private Long auditId;

    @Column(name = "EVENT_TIME")
    private LocalDateTime eventTime;

    @Column(name = "MODULE_CODE", length = 30)
    private String moduleCode;

    @Column(name = "ACTION_TYPE", length = 50)
    private String actionType;

    @Column(name = "ENTITY_TYPE", length = 50)
    private String entityType;

    @Column(name = "ENTITY_ID", length = 100)
    private String entityId;

    @Column(name = "BUSINESS_REF", length = 100)
    private String businessRef;

    @Lob
    @Column(name = "CHANGED_FIELDS", columnDefinition = "CLOB")
    private String changedFields;

    @Lob
    @Column(name = "OLD_VALUE", columnDefinition = "CLOB")
    private String oldValue;

    @Lob
    @Column(name = "NEW_VALUE", columnDefinition = "CLOB")
    private String newValue;

    @Column(name = "ACTOR_ID", length = 60)
    private String actorId;

    @Column(name = "CLIENT_IP", length = 45)
    private String clientIp;

    @Column(name = "CLIENT_PC_NAME", length = 100)
    private String clientPcName;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "APPROVAL_REQUEST_ID")
    private Long approvalRequestId;

    @Column(name = "CORRELATION_ID", length = 100)
    private String correlationId;

    @Column(name = "REMARKS", length = 500)
    private String remarks;
}

