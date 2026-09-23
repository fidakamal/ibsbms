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
@Table(name = "T_TRANS_AUTH")
public class TransAuth {

    @Id
    @Column(name = "OID", length = 20, nullable = false)
    private String oid;

    @Column(name = "FOLIO_BO", length = 16)
    private String folioBo;

    @Column(name = "TR_ID", length = 20)
    private String trId;

    @Column(name = "TR_CODE", length = 10)
    private String trCode;

    @Column(name = "TR_DATE")
    private LocalDate trDate;

    @Column(name = "DR_AMT", precision = 20, scale = 8)
    private BigDecimal drAmt;

    @Column(name = "CR_AMT", precision = 20, scale = 8)
    private BigDecimal crAmt;


    @Column(name = "TR_STATE", precision = 1)
    private Integer trState;

    @Column(name = "MAKER_ID", length = 60)
    private String makerId;

    @Column(name = "MAKER_IP", length = 20)
    private String makerIp;

    @Column(name = "CHECKER_ID", length = 60)
    private String checkerId;

    @Column(name = "CHECKER_IP", length = 20)
    private String checkerIp;

    @Column(name = "REF_INSTR_NO", length = 20)
    private String refInstrNo;

    @Column(name = "CONTRA_ACC_NO", columnDefinition = "CHAR(17)")
    private String contraAccNo;

    @Column(name = "INSTR_NO", length = 20)
    private String instrNo;

    @Column(name = "INSTR_TYPE", length = 2)
    private String instrType;

    @Column(name = "INSTR_DATE")
    private LocalDate instrDate;

    @Column(name = "PARTICULAR", length = 255)
    private String particular;

    @Column(name = "REMARKS", length = 255)
    private String remarks;

    @Column(name = "MODIFY_DATE")
    private LocalDateTime modifyDate;
}