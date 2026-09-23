package com.example.ibsbms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_ACCOUNT_CDBL")
public class AccountCdbl {

    @Id
    @Column(name = "BO_NO", length = 16)
    private String boNo;

    @Column(name = "BO_NAME", length = 100)
    private String boName;

    @Column(name = "FREE_BALANCE", precision = 20, scale = 2)
    private BigDecimal freeBalance;

    @Column(name = "PLEDGE_BALANCE", precision = 20, scale = 2)
    private BigDecimal pledgeBalance;

    @Column(name = "LOCKIN_BALANCE", precision = 20, scale = 2)
    private BigDecimal lockinBalance;

    @Column(name = "CURRENT_BALANCE", precision = 20, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "FROZEN_BALANCE", precision = 20, scale = 2)
    private BigDecimal frozenBalance;

    @Column(name = "BO_STATUS", length = 21)
    private String boStatus;

    @Column(name = "SUSPENSION_STATUS", length = 20)
    private String suspensionStatus;
}