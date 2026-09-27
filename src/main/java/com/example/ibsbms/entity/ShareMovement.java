package com.example.ibsbms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_SHARE_MOVEMENT")
public class ShareMovement {

    @Id
    @Column(name = "MOVEMENT_ID", nullable = false)
    private Long movementId;

    @Column(name = "MOVEMENT_REF", length = 30, nullable = false)
    private String movementRef;

    @Column(name = "MOVEMENT_TYPE", length = 20, nullable = false)
    private String movementType;

    @Column(name = "SOURCE_TYPE", length = 20)
    private String sourceType;

    @Column(name = "SOURCE_REF", length = 50)
    private String sourceRef;

    @Column(name = "TARGET_TYPE", length = 20)
    private String targetType;

    @Column(name = "TARGET_REF", length = 50)
    private String targetRef;

    @Column(name = "QUANTITY", precision = 16, scale = 0)
    private BigDecimal quantity;

    @Column(name = "PARTICULARS", length = 255)
    private String particulars;

    @Column(name = "INSTRUMENT", length = 100)
    private String instrument;

    @Column(name = "ORIGINAL_TR_ID", length = 80)
    private String originalTrId;

    @Column(name = "POSTED_TR_ID", length = 80)
    private String postedTrId;

    @Column(name = "GROUP_TR_ID", length = 80)
    private String groupTrId;

    @Column(name = "LOCAL_STATUS", length = 30)
    private String localStatus;

    @Column(name = "CDBL_STATUS", length = 30)
    private String cdblStatus;

    @Column(name = "REQUESTED_BY", length = 60)
    private String requestedBy;

    @Column(name = "REQUESTED_IP", length = 50)
    private String requestedIp;

    @Column(name = "REQUESTED_AT")
    private LocalDateTime requestedAt;

    @Column(name = "POSTED_BY", length = 60)
    private String postedBy;

    @Column(name = "POSTED_AT")
    private LocalDateTime postedAt;

    @Column(name = "BUSINESS_DATE")
    private LocalDate businessDate;
}