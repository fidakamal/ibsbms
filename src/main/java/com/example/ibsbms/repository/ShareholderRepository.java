package com.example.ibsbms.repository;

import com.example.ibsbms.entity.Shareholder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareholderRepository
        extends JpaRepository<Shareholder, String> {

    Optional<Shareholder> findByFolioBo(String folioBo);
}