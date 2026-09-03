package com.example.ibsbms.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_BANKINFO_SHARE")
public class ShareBankInfo {

    @Id
    @Column(name = "OID", length = 20, nullable = false)
    private String oid;

    @Column(name = "FOLIO_BO", length = 16)
    private String folioBo;

    @Column(name = "ACC_NO", length = 17)
    private String accNo;

    @Column(name = "BANK_NAME", length = 80)
    private String bankName;

    @Column(name = "BRANCH_NAME", length = 80)
    private String branchName;

    @Column(name = "ROUTING_NO", length = 20)
    private String routingNo;
}
