package com.example.ibsbms.service;

import com.example.ibsbms.dto.BoSearchResult;
import com.example.ibsbms.dto.FolioSearchResult;
import com.example.ibsbms.entity.AccountCdbl;
import com.example.ibsbms.entity.Shareholder;
import com.example.ibsbms.exception.AccountNotFoundException;
import com.example.ibsbms.repository.AccountCdblRepository;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountSearchService {

    private final ShareholderRepository shareholderRepository;
    private final AccountCdblRepository accountCdblRepository;

    public AccountSearchService(
            ShareholderRepository shareholderRepository,
            AccountCdblRepository accountCdblRepository) {

        this.shareholderRepository = shareholderRepository;
        this.accountCdblRepository = accountCdblRepository;
    }

    public FolioSearchResult searchFolio(String folioNo) {

        if (folioNo == null || folioNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Folio No is required.");
        }

        Shareholder shareholder = shareholderRepository
                .findByFolioBoAndIsValid(folioNo.trim(), 1)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Folio not found or not active: " + folioNo
                ));

        return FolioSearchResult.from(shareholder);
    }

    public BoSearchResult searchBo(String boNo) {

        if (boNo == null || boNo.trim().isEmpty()) {
            throw new IllegalArgumentException("BO No is required.");
        }

        AccountCdbl account = accountCdblRepository
                .findByBoNo(boNo.trim())
                .orElseThrow(() -> new AccountNotFoundException(
                        "BO not found: " + boNo
                ));

        return BoSearchResult.from(account);
    }
}