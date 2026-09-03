package com.example.ibsbms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_APPROVAL_REQUEST")
public class ApprovalRequest {

    @Id
    @Column(name = "REQUEST_ID", nullable = false)
    private Long requestId;

    @Column(name = "OPERATION_CODE", length = 50, nullable = false)
    private String operationCode;

    @Column(name = "ENTITY_TYPE", length = 40, nullable = false)
    private String entityType;

    @Column(name = "ENTITY_ID", length = 50)
    private String entityId;

    @Column(name = "SOURCE_TYPE", length = 40, nullable = false)
    private String sourceType;

    @Column(name = "SOURCE_ID", length = 50, nullable = false)
    private String sourceId;

    @Column(name = "BUSINESS_REF", length = 50)
    private String businessRef;

    @Column(name = "STATUS", length = 30, nullable = false)
    private String status;

    @Column(name = "CURRENT_STAGE", length = 20, nullable = false)
    private String currentStage;

    @Column(name = "MAKER_ID", length = 60, nullable = false)
    private String makerId;

    @Column(name = "MAKER_IP", length = 50)
    private String makerIp;

    @Column(name = "CHECKER_ID", length = 60)
    private String checkerId;

    @Column(name = "CHECKER_IP", length = 50)
    private String checkerIp;

    @Column(name = "APPROVER_ID", length = 60)
    private String approverId;

    @Column(name = "APPROVER_IP", length = 50)
    private String approverIp;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "DECIDED_AT")
    private LocalDateTime decidedAt;

    @Column(name = "VERSION_NO", precision = 10)
    private Integer versionNo;

    @Column(name = "BUSINESS_DATE")
    private LocalDate businessDate;
}