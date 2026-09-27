package com.example.ibsbms.repository;

import com.example.ibsbms.entity.CdblOutItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdblOutItemRepository
        extends JpaRepository<CdblOutItem, Long> {
}