package com.example.ibsbms.repository;

import com.example.ibsbms.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, String> {

    List<Certificate> findByFolioNoOrderByTrDateDesc(String folioNo);
}