package com.example.ibsbms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_CDBL_OUT_BATCH")
public class CdblOutBatch {

    @Id
    @Column(name = "OUT_BATCH_ID", nullable = false)
    private Long outBatchId;

    @Column(name = "BATCH_REF", length = 30)
    private String batchRef;

    @Column(name = "STATUS", length = 30)
    private String status;

    @Column(name = "APPROVAL_REQUEST_ID")
    private Long approvalRequestId;

    @Column(name = "MOVEMENT_COUNT")
    private Long movementCount;

    @Column(name = "TOTAL_QUANTITY")
    private Long totalQuantity;

    @Column(name = "CREATED_BY", length = 60)
    private String createdBy;

    @Column(name = "CREATED_IP", length = 50)
    private String createdIp;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "VERSION_NO")
    private Integer versionNo;
}