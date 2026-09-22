package com.example.ibsbms.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "OID", length = 20, nullable = false)
    private String oid;

    @Column(name = "FOLIO_BO", length = 16)
    private String folioBo;

    /*
     * Human-facing transaction reference, e.g. "TR202600123"
     * (see Previous Transactions panel in the reference screenshot).
     */
    @Column(name = "TR_ID", length = 20)
    private String trId;

    /*
     * Links the debit leg and the credit leg of one transfer together.
     * PDF section 11: "Both rows use the same GRP_TR_ID."
     */
    @Column(name = "GRP_TR_ID", length = 20)
    private String grpTrId;

    @Column(name = "TR_DATE")
    private LocalDate trDate;

    /*
     * Always "TRANSFER" for this module per the confirmed
     * Transaction Type Mapping (see TransferType enum).
     */
    @Column(name = "TR_TYPE", length = 20)
    private String trType;

    /*
     * F2F / F2BO / BO2F - see TransferType enum.
     */
    @Column(name = "TR_CODE", length = 10)
    private String trCode;

    @Column(name = "DR_SHARE", precision = 16)
    private Long drShare;

    @Column(name = "CR_SHARE", precision = 16)
    private Long crShare;


    @Column(name = "CONTRA_ACC", length = 16)
    private String contraAcc;

    @Column(name = "INSTRUMENT", length = 50)
    private String instrument;

    @Column(name = "PARTICULARS", length = 200)
    private String particulars;

    /*
     * Acting user at posting time - resolved server-side from the
     * authenticated principal, never trusted from the client
     * (PDF section 15, "Non-Negotiable Rule" list item 6).
     */
    @Column(name = "USER_ID", length = 60)
    private String userId;

    @Column(name = "IS_VALID", precision = 1)
    private Integer isValid;

    @Column(name = "POST_DATE")
    private LocalDateTime postDate;
}