package com.example.ibsbms.repository;

import com.example.ibsbms.entity.TransShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransShareRepository extends JpaRepository<TransShare, String> {

    List<TransShare> findByFolioBoOrderByTrDateDesc(String folioBo);

    List<TransShare> findByGrpTrId(String grpTrId);
}