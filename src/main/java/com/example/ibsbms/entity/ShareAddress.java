package com.example.ibsbms.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_ADDRESS_SHARE")
public class ShareAddress {

    @Id
    @Column(name = "OID", length = 20, nullable = false)
    private String oid;

    @Column(name = "FOLIO_BO", length = 16)
    private String folioBo;

    @Column(name = "ADD1", length = 80)
    private String add1;

    @Column(name = "ADD2", length = 80)
    private String add2;

    @Column(name = "ADD3", length = 80)
    private String add3;

    @Column(name = "ADD4", length = 80)
    private String add4;

    @Column(name = "COUNTRY_NAME", length = 60)
    private String countryName;
}
