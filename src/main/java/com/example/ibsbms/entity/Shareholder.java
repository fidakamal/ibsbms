package com.example.ibsbms.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_ACCOUNT_SHARE")
public class Shareholder {

    @Id
    @Column(name = "OID", length = 20, nullable = false)
    private String oid;

    @Column(name = "FOLIO_BO", length = 16)
    private String folioBo;

    @Column(name = "CUST_NAME", length = 100)
    private String custName;

    @Column(name = "FATHER_NAME", length = 60)
    private String fatherName;

    @Column(name = "MOTHER_NAME", length = 60)
    private String motherName;

    @Column(name = "SPOUSE_NAME", length = 60)
    private String spouseName;

    @Column(name = "REPRESENTATIVE", length = 80)
    private String representative;

    @Column(name = "CUST_TYPE", precision = 1)
    private Integer custType;

    @Column(name = "CITIZEN_TYPE", precision = 1)
    private Integer citizenType;

    @Column(name = "RESIDENT_TYPE", length = 15)
    private String residentType;

    @Column(name = "PHONE", length = 11)
    private String phone;

    @Column(name = "EMAIL", length = 60)
    private String email;

    @Column(name = "DOB")
    private LocalDate dob;

    @Column(name = "REGISTRATION_DATE")
    private LocalDate registrationDate;

    @Column(name = "IS_VALID", precision = 1)
    private Integer isValid;

    @Column(name = "IS_EMPLOYEE", precision = 1)
    private Integer isEmployee;

    @Column(name = "NID_NO", length = 17)
    private String nidNo;

    @Column(name = "TIN_NO", length = 12)
    private String tinNo;

    @Column(name = "ICB_CODE", precision = 3)
    private Integer icbCode;

    @Column(name = "IS_LIEN", precision = 1)
    private Integer isLien;

    @Column(name = "SHARES", precision = 16)
    private Long shares;

    @Column(name = "SUSPENSE", precision = 16)
    private Long suspense;

    @Column(name = "BONUS", precision = 16)
    private Long bonus;

    @Column(name = "BALANCE", precision = 16)
    private Long balance;

    @Column(name = "MAKER_ID", length = 60)
    private String makerId;

    @Column(name = "CHECKER_ID", length = 60)
    private String checkerId;

    @Column(name = "STATUS", precision = 2)
    private Integer status;

    @Column(name = "OLD_BO", length = 17)
    private String oldBo;
}
