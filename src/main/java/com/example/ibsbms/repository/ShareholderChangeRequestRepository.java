package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ShareholderChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareholderChangeRequestRepository
        extends JpaRepository<ShareholderChangeRequest, String> {
}
