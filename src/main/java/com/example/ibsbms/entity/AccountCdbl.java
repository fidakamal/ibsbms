package com.example.ibsbms.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_ACCOUNT_CDBL")
public class AccountCdbl {

    @Id
    @Column(name = "BO_NO", length = 16, nullable = false)
    private String boNo;

    @Column(name = "BO_NAME", length = 100)
    private String boName;

    @Column(name = "FREE_BALANCE", precision = 16)
    private Long freeBalance;

    @Column(name = "PLEDGE_BALANCE", precision = 16)
    private Long pledgeBalance;

    @Column(name = "LOCKIN_BALANCE", precision = 16)
    private Long lockinBalance;

    @Column(name = "CURRENT_BALANCE", precision = 16)
    private Long currentBalance;

    @Column(name = "FROZEN_BALANCE", precision = 16)
    private Long frozenBalance;

    /*
     * Type unconfirmed against real DDL - PDF only names the column,
     * never its type. Verify before relying on this as String.
     */
    @Column(name = "BO_STATUS", length = 20)
    private String boStatus;

    /*
     * Type unconfirmed against real DDL - same caveat as BO_STATUS.
     */
    @Column(name = "SUSPENSION_STATUS", length = 20)
    private String suspensionStatus;
}