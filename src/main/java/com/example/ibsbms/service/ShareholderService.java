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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShareholderService {

    private final ObjectMapper objectMapper;
    private final ShareholderRepository shareholderRepository;
    private final ShareAddressRepository shareAddressRepository;
    private final ShareBankInfoRepository shareBankInfoRepository;

    public ShareholderService(
            ObjectMapper objectMapper,
            ShareholderRepository shareholderRepository,
            ShareAddressRepository shareAddressRepository,
            ShareBankInfoRepository shareBankInfoRepository) {

        this.objectMapper = objectMapper;
        this.shareholderRepository = shareholderRepository;
        this.shareAddressRepository = shareAddressRepository;
        this.shareBankInfoRepository = shareBankInfoRepository;
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

    /*
     * Loads the CURRENT APPROVED master data for a Folio/BO
     * (T_ACCOUNT_SHARE + T_ADDRESS_SHARE + T_BANKINFO_SHARE) and maps it
     * into the same DTO shape used by the Create form.
     */
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
}
