package com.example.ibsbms.service;

import com.example.ibsbms.dto.ShareholderCreateRequest;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShareholderService {

    private final ObjectMapper objectMapper;

    public ShareholderService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildCreateProposalJson(ShareholderCreateRequest request) {

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("version", 1);
        payload.put("basicInfo", request.getBasicInfo());
        payload.put("address", request.getAddress());
        payload.put("bankInfo", request.getBankInfo());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Unable to create shareholder proposal JSON", e
            );
        }
    }
}