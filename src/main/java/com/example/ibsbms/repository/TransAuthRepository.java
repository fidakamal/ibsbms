package com.example.ibsbms.repository;

import com.example.ibsbms.entity.TransAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransAuthRepository extends JpaRepository<TransAuth, String> {
}