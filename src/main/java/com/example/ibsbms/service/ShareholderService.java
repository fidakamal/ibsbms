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

import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

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

        String trimmed = json.trim();

        /*
         * ============================================================
         * FORMAT 1: CURRENT JSON
         * ============================================================
         *
         * {
         *   "version": 1,
         *   "basicInfo": {...},
         *   "address": {...},
         *   "bankInfo": {...}
         * }
         */
        if (trimmed.startsWith("{")) {

            try {

                JsonNode root =
                        objectMapper.readTree(trimmed);

                /*
                 * Current proposal format
                 */
                if (root.has("basicInfo")) {

                    return objectMapper.treeToValue(
                            root,
                            ShareholderCreateRequest.class
                    );
                }

                /*
                 * ====================================================
                 * FORMAT 2: OLD FLAT JSON
                 * ====================================================
                 *
                 * {
                 *   "folioBo": "...",
                 *   "custName": "...",
                 *   ...
                 *   "addressDto": {...},
                 *   "bankInfoShareDto": {...}
                 * }
                 */

                ShareholderCreateRequest request =
                        new ShareholderCreateRequest();

                BasicInfoDto basicInfo =
                        new BasicInfoDto();

                basicInfo.setFolioBo(
                        root.path("folioBo").asText(null)
                );

                basicInfo.setCustName(
                        root.path("custName").asText(null)
                );

                basicInfo.setFatherName(
                        root.path("fatherName").asText(null)
                );

                basicInfo.setMotherName(
                        root.path("motherName").asText(null)
                );

                basicInfo.setSpouseName(
                        root.path("spouseName").asText(null)
                );

                basicInfo.setRepresentative(
                        root.path("representativeName").asText(null)
                );

                basicInfo.setCustType(
                        root.hasNonNull("custType")
                                ? root.get("custType").asInt()
                                : null
                );

                basicInfo.setCitizenType(
                        root.hasNonNull("citizenType")
                                ? root.get("citizenType").asInt()
                                : null
                );

                basicInfo.setResidentType(
                        root.path("residentType").asText(null)
                );

                basicInfo.setPhone(
                        root.path("phone").asText(null)
                );

                basicInfo.setEmail(
                        root.path("email").asText(null)
                );

                basicInfo.setIsEmployee(
                        root.hasNonNull("isEmployee")
                                ? root.get("isEmployee").asInt()
                                : null
                );

                basicInfo.setNidNo(
                        root.path("nidNo").asText(null)
                );

                basicInfo.setTinNo(
                        root.path("tinNo").asText(null)
                );

                basicInfo.setIcbCode(
                        root.hasNonNull("icbCode")
                                ? root.get("icbCode").asInt()
                                : null
                );

                if (root.hasNonNull("dateOfBirth")) {

                    String dateOfBirth =
                            root.get("dateOfBirth").asText();

                    if (!dateOfBirth.isBlank()) {
                        basicInfo.setDob(
                                LocalDate.parse(dateOfBirth)
                        );
                    }
                }

                request.setBasicInfo(basicInfo);

                AddressDto address =
                        new AddressDto();

                JsonNode addressNode =
                        root.path("addressDto");

                address.setAdd1(
                        addressNode.path("add1").asText(null)
                );

                address.setAdd2(
                        addressNode.path("add2").asText(null)
                );

                address.setAdd3(
                        addressNode.path("add3").asText(null)
                );

                address.setAdd4(
                        addressNode.path("add4").asText(null)
                );

                address.setCountryName(
                        addressNode.path("countryName").asText(null)
                );

                request.setAddress(address);

                BankInfoDto bankInfo =
                        new BankInfoDto();

                JsonNode bankNode =
                        root.path("bankInfoShareDto");

                bankInfo.setAccNo(
                        bankNode.path("accNo").asText(null)
                );

                bankInfo.setBankName(
                        bankNode.path("bankName").asText(null)
                );

                bankInfo.setBranchName(
                        bankNode.path("branchName").asText(null)
                );

                bankInfo.setRoutingNo(
                        bankNode.path("routingNo").asText(null)
                );

                request.setBankInfo(bankInfo);

                return request;
            }

            catch (JacksonException |
                   java.time.format.DateTimeParseException e) {

                throw new IllegalStateException(
                        "Unable to parse shareholder proposal JSON",
                        e
                );
            }
        }


        /*
         * ============================================================
         * FORMAT 3: VERY OLD ShareholderFormDTO.toString()
         * ============================================================
         *
         * Example:
         *
         * ShareholderFormDTO(
         *     folioBo=11,
         *     custName=Rafi cagol,
         *     phone=01629676950,
         *     ...
         * )
         */
        if (trimmed.startsWith("ShareholderFormDTO(")) {

            return parseLegacyShareholderFormDto(trimmed);
        }


        throw new IllegalStateException(
                "Unknown shareholder proposal format."
        );
    }




    private ShareholderCreateRequest parseLegacyShareholderFormDto(
            String text) {

        /*
         * Remove:
         *
         * ShareholderFormDTO(
         *        ...
         * )
         */
        String body = text.substring(
                "ShareholderFormDTO(".length(),
                text.endsWith(")") ? text.length() - 1 : text.length()
        );

        /*
         * Extract key=value pairs.
         *
         * Important:
         * Values themselves may contain commas.
         *
         * Example:
         *
         * add1=79/c/5, Uttor Jatrabari,Dhaka,
         * add2=,
         *
         * So we cannot simply split on ",".
         *
         * Instead we look for:
         *
         * , nextField=
         */
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(
                        "(\\w+)=((?:(?!,\\s+\\w+=).)*)"
                );

        java.util.regex.Matcher matcher =
                pattern.matcher(body);

        java.util.Map<String, String> values =
                new java.util.LinkedHashMap<>();

        while (matcher.find()) {

            String key = matcher.group(1);
            String value = matcher.group(2);

            values.put(
                    key,
                    cleanLegacyValue(value)
            );
        }


        ShareholderCreateRequest request =
                new ShareholderCreateRequest();


        /*
         * ============================================================
         * BASIC INFORMATION
         * ============================================================
         */

        BasicInfoDto basicInfo =
                new BasicInfoDto();

        basicInfo.setFolioBo(
                values.get("folioBo")
        );

        basicInfo.setCustName(
                values.get("custName")
        );

        basicInfo.setFatherName(
                values.get("fatherName")
        );

        basicInfo.setMotherName(
                values.get("motherName")
        );

        basicInfo.setSpouseName(
                values.get("spouseName")
        );

        /*
         * Older DTO may use representativeName.
         */
        basicInfo.setRepresentative(
                values.get("representativeName")
        );

        basicInfo.setPhone(
                values.get("phone")
        );

        basicInfo.setEmail(
                values.get("email")
        );

        basicInfo.setResidentType(
                values.get("residentType")
        );

        basicInfo.setCustType(
                parseInteger(values.get("custType"))
        );

        basicInfo.setCitizenType(
                parseInteger(values.get("citizenType"))
        );

        basicInfo.setIsEmployee(
                parseInteger(values.get("isEmployee"))
        );

        basicInfo.setNidNo(
                values.get("nidNo")
        );

        basicInfo.setTinNo(
                values.get("tinNo")
        );

        basicInfo.setIcbCode(
                parseInteger(values.get("icbCode"))
        );

        request.setBasicInfo(basicInfo);


        /*
         * ============================================================
         * ADDRESS
         * ============================================================
         */

        AddressDto address =
                new AddressDto();

        address.setAdd1(
                values.get("add1")
        );

        address.setAdd2(
                values.get("add2")
        );

        address.setAdd3(
                values.get("add3")
        );

        address.setAdd4(
                values.get("add4")
        );

        address.setCountryName(
                values.get("countryName")
        );

        request.setAddress(address);


        /*
         * ============================================================
         * BANK INFORMATION
         * ============================================================
         *
         * The old ShareholderFormDTO does not appear to contain
         * bank information, so we simply provide an empty DTO.
         */

        BankInfoDto bankInfo =
                new BankInfoDto();

        request.setBankInfo(bankInfo);


        return request;
    }



    private String cleanLegacyValue(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        if ("null".equalsIgnoreCase(cleaned)) {
            return null;
        }

        return cleaned;
    }





    private Integer parseInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
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