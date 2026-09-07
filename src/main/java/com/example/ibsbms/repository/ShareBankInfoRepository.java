package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ShareBankInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareBankInfoRepository
        extends JpaRepository<ShareBankInfo, String> {
}

