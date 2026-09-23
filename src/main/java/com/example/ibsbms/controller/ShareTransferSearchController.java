package com.example.ibsbms.controller;

import com.example.ibsbms.dto.BoSearchResult;
import com.example.ibsbms.dto.FolioSearchResult;
import com.example.ibsbms.exception.AccountNotFoundException;
import com.example.ibsbms.service.AccountSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ShareTransferSearchController {

    private final AccountSearchService accountSearchService;

    public ShareTransferSearchController(AccountSearchService accountSearchService) {
        this.accountSearchService = accountSearchService;
    }

    @GetMapping("/share-transfer/search/folio/{folioNo}")
    public FolioSearchResult searchFolio(@PathVariable String folioNo) {
        return accountSearchService.searchFolio(folioNo);
    }

    @GetMapping("/share-transfer/search/bo/{boNo}")
    public BoSearchResult searchBo(@PathVariable String boNo) {
        return accountSearchService.searchBo(boNo);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(AccountNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}