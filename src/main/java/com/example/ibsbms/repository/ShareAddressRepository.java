package com.example.ibsbms.repository;

import com.example.ibsbms.entity.ShareAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareAddressRepository extends JpaRepository<ShareAddress, String> {

    Optional<ShareAddress> findByFolioBo(String folioBo);
}
