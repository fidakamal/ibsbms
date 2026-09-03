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
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "T_SHAREHOLDER_CHANGE_REQUEST")
public class ShareholderChangeRequest {

    @Id
    @Column(name = "CHANGE_ID", length = 20, nullable = false)
    private String changeId;

    @Column(name = "FOLIO_BO", length = 16)
    private String folioBo;

    @Column(name = "OPERATION_CODE", length = 50, nullable = false)
    private String operationCode;

    @Lob
    @Column(name = "OLD_VALUE")
    private String oldValue;

    @Lob
    @Column(name = "NEW_VALUE", nullable = false)
    private String newValue;

    @Column(name = "CREATED_BY", length = 60, nullable = false)
    private String createdBy;

    @Column(name = "CREATED_IP", length = 45)
    private String createdIp;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "VERSION_NO", nullable = false)
    private Integer versionNo = 0;
}
