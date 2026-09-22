package com.example.ibsbms.repository;

import com.example.ibsbms.entity.AccountCdbl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountCdblRepository extends JpaRepository<AccountCdbl, String> {

    Optional<AccountCdbl> findByBoNo(String boNo);
}