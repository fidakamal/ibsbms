package com.example.ibsbms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_CERTIFICATE")
public class Certificate {

    @Id
    @Column(name = "OID", length = 20, nullable = false)
    private String oid;

    @Column(name = "FOLIO_NO", length = 16, nullable = false)
    private String folioNo;

    @Column(name = "CERTI_NO", length = 120, nullable = false)
    private String certiNo;

    @Column(name = "TR_DATE")
    private LocalDate trDate;

    @Column(name = "DIST_FROM", length = 10, nullable = false)
    private String distFrom;

    @Column(name = "DIST_TO", length = 10, nullable = false)
    private String distTo;

    @Column(name = "IS_VALID", precision = 1)
    private Integer isValid;

    @Column(name = "USER_ID", length = 60, nullable = false)
    private String userId;
}