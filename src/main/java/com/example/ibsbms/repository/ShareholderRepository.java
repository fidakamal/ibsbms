package com.example.ibsbms.repository;

import com.example.ibsbms.entity.Shareholder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareholderRepository
        extends JpaRepository<Shareholder, String> {
}