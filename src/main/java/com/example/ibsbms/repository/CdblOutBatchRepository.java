package com.example.ibsbms.repository;

import com.example.ibsbms.entity.CdblOutBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdblOutBatchRepository
        extends JpaRepository<CdblOutBatch, Long> {
}