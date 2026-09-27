package com.example.ibsbms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_CDBL_OUT_ITEM")
public class CdblOutItem {

    @Id
    @Column(name = "OUT_ITEM_ID", nullable = false)
    private Long outItemId;

    @Column(name = "OUT_BATCH_ID", nullable = false)
    private Long outBatchId;

    @Column(name = "MOVEMENT_ID", nullable = false)
    private Long movementId;

    @Column(name = "MOVEMENT_REF", length = 30)
    private String movementRef;

    @Column(name = "MOVEMENT_TYPE", length = 20)
    private String movementType;

    @Column(name = "FOLIO_NO", length = 17)
    private String folioNo;

    @Column(name = "BO_NO", length = 16)
    private String boNo;

    @Column(name = "QUANTITY")
    private Long quantity;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}