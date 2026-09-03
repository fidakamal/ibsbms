package com.example.ibsbms.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "T_APPROVAL_REQUEST")
public class ApprovalRequest {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "approvalRequestSeq"
    )
    @SequenceGenerator(
            name = "approvalRequestSeq",
            sequenceName = "SEQ_APPROVAL_REQUEST",
            allocationSize = 1
    )
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

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "DECIDED_AT")
    private LocalDateTime decidedAt;

    @Column(name = "VERSION_NO", nullable = false)
    private Integer versionNo = 0;

    @Column(name = "BUSINESS_DATE", nullable = false)
    private LocalDate businessDate;
}
