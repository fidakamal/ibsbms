package com.example.ibsbms.service;

import com.example.ibsbms.dto.AddressDto;
import com.example.ibsbms.dto.BankInfoDto;
import com.example.ibsbms.dto.BasicInfoDto;
import com.example.ibsbms.dto.ShareholderCreateRequest;
import com.example.ibsbms.entity.ShareAddress;
import com.example.ibsbms.entity.ShareBankInfo;
import com.example.ibsbms.entity.Shareholder;
import com.example.ibsbms.repository.ShareAddressRepository;
import com.example.ibsbms.repository.ShareBankInfoRepository;
import com.example.ibsbms.repository.ShareholderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShareholderService {

    private final ObjectMapper objectMapper;
    private final ShareholderRepository shareholderRepository;
    private final ShareAddressRepository shareAddressRepository;
    private final ShareBankInfoRepository shareBankInfoRepository;
    private final WorkflowIdService workflowIdService;

    public ShareholderService(
            ObjectMapper objectMapper,
            ShareholderRepository shareholderRepository,
            ShareAddressRepository shareAddressRepository,
            ShareBankInfoRepository shareBankInfoRepository,
            WorkflowIdService workflowIdService) {

        this.objectMapper = objectMapper;
        this.shareholderRepository = shareholderRepository;
        this.shareAddressRepository = shareAddressRepository;
        this.shareBankInfoRepository = shareBankInfoRepository;
        this.workflowIdService = workflowIdService;
    }

    public ShareholderCreateRequest parseProposalJson(String json) {

        if (json == null || json.isBlank()) {
            return new ShareholderCreateRequest();
        }

        try {
            return objectMapper.readValue(json, ShareholderCreateRequest.class);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Unable to parse shareholder proposal JSON", e);
        }
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
                    "Unable to create shareholder proposal JSON", e);
        }
    }

    public ShareholderCreateRequest snapshotOf(String folioBo) {

        Shareholder shareholder = shareholderRepository
                .findByFolioBoAndIsValid(folioBo, 1)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown or not-yet-approved Folio/BO: " + folioBo));

        ShareAddress address = shareAddressRepository
                .findByFolioBo(folioBo)
                .orElse(null);

        ShareBankInfo bankInfo = shareBankInfoRepository
                .findByFolioBo(folioBo)
                .orElse(null);

        BasicInfoDto basicInfo = new BasicInfoDto();
        basicInfo.setFolioBo(shareholder.getFolioBo());
        basicInfo.setCustName(shareholder.getCustName());
        basicInfo.setFatherName(shareholder.getFatherName());
        basicInfo.setMotherName(shareholder.getMotherName());
        basicInfo.setSpouseName(shareholder.getSpouseName());
        basicInfo.setRepresentative(shareholder.getRepresentative());
        basicInfo.setCustType(shareholder.getCustType());
        basicInfo.setCitizenType(shareholder.getCitizenType());
        basicInfo.setResidentType(shareholder.getResidentType());
        basicInfo.setPhone(shareholder.getPhone());
        basicInfo.setEmail(shareholder.getEmail());
        basicInfo.setDob(shareholder.getDob());
        basicInfo.setIsEmployee(shareholder.getIsEmployee());
        basicInfo.setNidNo(shareholder.getNidNo());
        basicInfo.setTinNo(shareholder.getTinNo());
        basicInfo.setIcbCode(shareholder.getIcbCode());

        AddressDto addressDto = new AddressDto();
        if (address != null) {
            addressDto.setAdd1(address.getAdd1());
            addressDto.setAdd2(address.getAdd2());
            addressDto.setAdd3(address.getAdd3());
            addressDto.setAdd4(address.getAdd4());
            addressDto.setCountryName(address.getCountryName());
        }

        BankInfoDto bankInfoDto = new BankInfoDto();
        if (bankInfo != null) {
            bankInfoDto.setAccNo(bankInfo.getAccNo());
            bankInfoDto.setBankName(bankInfo.getBankName());
            bankInfoDto.setBranchName(bankInfo.getBranchName());
            bankInfoDto.setRoutingNo(bankInfo.getRoutingNo());
        }

        ShareholderCreateRequest snapshot = new ShareholderCreateRequest();
        snapshot.setBasicInfo(basicInfo);
        snapshot.setAddress(addressDto);
        snapshot.setBankInfo(bankInfoDto);

        return snapshot;
    }

    @Transactional
    public void applyApproved(
            String folioBo,
            ShareholderCreateRequest data,
            String makerId,
            String checkerId) {

        BasicInfoDto basicInfo = data.getBasicInfo();
        AddressDto addressDto = data.getAddress();
        BankInfoDto bankInfoDto = data.getBankInfo();

        Shareholder shareholder = shareholderRepository
                .findByFolioBo(folioBo)
                .orElseGet(Shareholder::new);

        boolean isNewShareholder = shareholder.getOid() == null;

        if (isNewShareholder) {
            shareholder.setOid(workflowIdService.generateChangeId());
            shareholder.setFolioBo(folioBo);
            shareholder.setRegistrationDate(LocalDate.now(ZoneId.of("Asia/Dhaka")));
        }

        shareholder.setCustName(basicInfo.getCustName());
        shareholder.setFatherName(basicInfo.getFatherName());
        shareholder.setMotherName(basicInfo.getMotherName());
        shareholder.setSpouseName(basicInfo.getSpouseName());
        shareholder.setRepresentative(basicInfo.getRepresentative());
        shareholder.setCustType(basicInfo.getCustType());
        shareholder.setCitizenType(basicInfo.getCitizenType());
        shareholder.setResidentType(basicInfo.getResidentType());
        shareholder.setPhone(basicInfo.getPhone());
        shareholder.setEmail(basicInfo.getEmail());
        shareholder.setDob(basicInfo.getDob());
        shareholder.setIsEmployee(basicInfo.getIsEmployee());
        shareholder.setNidNo(basicInfo.getNidNo());
        shareholder.setTinNo(basicInfo.getTinNo());
        shareholder.setIcbCode(basicInfo.getIcbCode());
        shareholder.setIsValid(1);
        shareholder.setMakerId(makerId);
        shareholder.setCheckerId(checkerId);

        shareholderRepository.save(shareholder);

        ShareAddress address = shareAddressRepository
                .findByFolioBo(folioBo)
                .orElseGet(ShareAddress::new);

        if (address.getOid() == null) {
            address.setOid(workflowIdService.generateChangeId());
            address.setFolioBo(folioBo);
        }

        address.setAdd1(addressDto.getAdd1());
        address.setAdd2(addressDto.getAdd2());
        address.setAdd3(addressDto.getAdd3());
        address.setAdd4(addressDto.getAdd4());
        address.setCountryName(addressDto.getCountryName());

        shareAddressRepository.save(address);

        boolean hasBankInfo = bankInfoDto != null && (
                hasText(bankInfoDto.getBankName())
                        || hasText(bankInfoDto.getAccNo())
                        || hasText(bankInfoDto.getBranchName())
                        || hasText(bankInfoDto.getRoutingNo()));

        if (hasBankInfo) {
            ShareBankInfo bankInfo = shareBankInfoRepository
                    .findByFolioBo(folioBo)
                    .orElseGet(ShareBankInfo::new);

            if (bankInfo.getOid() == null) {
                bankInfo.setOid(workflowIdService.generateChangeId());
                bankInfo.setFolioBo(folioBo);
            }

            bankInfo.setBankName(bankInfoDto.getBankName());
            bankInfo.setAccNo(bankInfoDto.getAccNo());
            bankInfo.setBranchName(bankInfoDto.getBranchName());
            bankInfo.setRoutingNo(bankInfoDto.getRoutingNo());

            shareBankInfoRepository.save(bankInfo);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}