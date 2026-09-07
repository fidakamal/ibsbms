package com.example.ibsbms.repository;

import com.example.ibsbms.entity.BusinessAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessAuditRepository
        extends JpaRepository<BusinessAudit, Long> {
}

