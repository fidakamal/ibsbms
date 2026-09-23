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
@Table(name = "T_TRANS_SHARE")
public class TransShare {

    @Id
    @Column(name = "OID", precision = 20, scale = 0, nullable = false)
    private Long oid;

    @Column(name = "FOLIO_BO", length = 16, nullable = false)
    private String folioBo;

    @Column(name = "TR_ID", length = 80, nullable = false)
    private String trId;

    @Column(name = "GRP_TR_ID", length = 80)
    private String grpTrId;

    @Column(name = "TR_DATE", nullable = false)
    private LocalDate trDate;

    @Column(name = "TR_TYPE", length = 80, nullable = false)
    private String trType;

    @Column(name = "TR_CODE", length = 40, nullable = false)
    private String trCode;

    @Column(name = "DR_SHARE", precision = 16, scale = 2)
    private BigDecimal drShare;

    @Column(name = "CR_SHARE", precision = 16, scale = 2)
    private BigDecimal crShare;

    @Column(name = "CONTRA_ACC", length = 64)
    private String contraAcc;

    @Column(name = "INSTRUMENT", length = 200)
    private String instrument;

    @Column(name = "PARTICULARS", length = 800, nullable = false)
    private String particulars;

    @Column(name = "USER_ID", length = 60, nullable = false)
    private String userId;

    @Column(name = "IS_VALID", precision = 1)
    private Integer isValid;

    @Column(name = "POST_DATE")
    private LocalDateTime postDate;
}