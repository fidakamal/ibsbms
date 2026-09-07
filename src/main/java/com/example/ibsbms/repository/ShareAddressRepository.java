package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ShareAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareAddressRepository
        extends JpaRepository<ShareAddress, String> {
}

