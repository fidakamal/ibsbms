package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ShareBankInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareBankInfoRepository extends JpaRepository<ShareBankInfo, String> {

    Optional<ShareBankInfo> findByFolioBo(String folioBo);
}
