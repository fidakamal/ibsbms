package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ShareMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareMovementRepository
        extends JpaRepository<ShareMovement, Long> {
}